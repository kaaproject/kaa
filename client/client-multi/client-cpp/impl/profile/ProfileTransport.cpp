/*
 * Copyright 2014-2016 CyberVision, Inc.
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

ProfileTransport::ProfileTransport(IKaaChannelManager& channelManager,
                                   const PublicKey& publicKey,
                                   IKaaClientContext &context)
    : AbstractKaaTransport(channelManager, context),
      profileManager_(nullptr), publicKey_(publicKey.begin(), publicKey.end())
{

}

bool ProfileTransport::isProfileOutDated(const HashDigest& profileHash)
{
    auto currentHash = context_.getStatus().getProfileHash();
    return profileHash != currentHash;
}

ProfileSyncRequestPtr ProfileTransport::createProfileRequest()
{
    ProfileSyncRequestPtr request;

    if (profileManager_) {
        auto encodedProfile = profileManager_->getSerializedProfile();
        HashDigest newHash = EndpointObjectHash(encodedProfile).getHashDigest();

        if (context_.getStatus().isProfileResyncNeeded()
                || !context_.getStatus().isRegistered() || isProfileOutDated(newHash))
        {
            context_.getStatus().setProfileHash(newHash);
            request.reset(new ProfileSyncRequest());
            request->endpointAccessToken.set_string(context_.getStatus().getEndpointAccessToken());

            if (!context_.getStatus().isRegistered()) {
                request->endpointPublicKey.set_bytes(publicKey_);
            } else {
                request->endpointPublicKey.set_null();
            }

            /* Profile */
            if (encodedProfile.second) {
                request->profileBody.assign(encodedProfile.first.get(), encodedProfile.first.get() + encodedProfile.second);
            }
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
        context_.getStatus().setProfileResyncNeeded(true);
        syncAll();
    } else if (!context_.getStatus().isRegistered()) {
        context_.getStatus().setRegistered(true);
    }

    KAA_LOG_INFO("Processed profile response");
}

} /* namespace kaa */
