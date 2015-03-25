/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "kaa/profile/ProfileTransport.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/profile/IProfileManager.hpp"

namespace kaa {

ProfileTransport::ProfileTransport(IKaaChannelManager& channelManager
        , const PublicKey& publicKey)
    : AbstractKaaTransport(channelManager), profileManager_(nullptr),
      publicKey_(publicKey.begin(), publicKey.end()) {}

bool ProfileTransport::isProfileOutDated(const HashDigest& profileHash)
{
    auto currentHash = clientStatus_->getProfileHash();
    return profileHash != currentHash;
}

ProfileSyncRequestPtr ProfileTransport::createProfileRequest()
{
    ProfileSyncRequestPtr request;

    if (clientStatus_ && profileManager_) {
        auto encodedProfile = profileManager_->getSerializedProfile();
        HashDigest newHash = EndpointObjectHash(encodedProfile).getHashDigest();
        if (isProfileOutDated(newHash) || !clientStatus_->isRegistered()) {
            clientStatus_->setProfileHash(newHash);
            request.reset(new ProfileSyncRequest());
            request->endpointAccessToken.set_string(clientStatus_->getEndpointAccessToken());

            if (!clientStatus_->isRegistered()) {
                request->endpointPublicKey.set_bytes(publicKey_);
            } else {
                request->endpointPublicKey.set_null();
            }

            /* Profile */
            if (encodedProfile.second) {
                request->profileBody.assign(encodedProfile.first.get(), encodedProfile.first.get() + encodedProfile.second);
            }

            /* Version info */
            request->versionInfo.configVersion = CONFIG_VERSION;
            request->versionInfo.profileVersion = PROFILE_VERSION;
            request->versionInfo.systemNfVersion = SYSTEM_NF_VERSION;
            request->versionInfo.userNfVersion = USER_NF_VERSION;
            request->versionInfo.logSchemaVersion = LOG_SCHEMA_VERSION;
            populateEventFamilyVersions(request->versionInfo.eventFamilyVersions);
        } else {
            KAA_LOG_INFO("Profile is up to date");
        }
    } else {
        KAA_LOG_ERROR("Failed to create ProfileSyncRequest");
    }

    return request;
}

void ProfileTransport::onProfileResponse(const ProfileSyncResponse& response)
{
    if (response.responseStatus == SyncResponseStatus::RESYNC) {
        KAA_LOG_INFO("Going to resync profile...");
        syncAll();
    } else if (clientStatus_ != nullptr && !clientStatus_->isRegistered()) {
        clientStatus_->setRegistered(true);
    }

    KAA_LOG_INFO("Processed profile response");
}

void ProfileTransport::populateEventFamilyVersions(EndpointVersionInfo::eventFamilyVersions_t& versions)
{
    static std::vector<EventClassFamilyVersionInfo> versionContainer;
    const auto& predefinedVersions = getEventClassFamilyVersionInfo();

    versions.set_null();

    if (!predefinedVersions.empty()) {
        versionContainer.reserve(predefinedVersions.size());

        for (const auto& version : predefinedVersions) {
            EventClassFamilyVersionInfo info;
            info.name = version.first;
            info.version = version.second;
            versionContainer.push_back(info);
        }
    }

    if (!versionContainer.empty()) {
        versions.set_array(versionContainer);
    }
}

} /* namespace kaa */
