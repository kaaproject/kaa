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

#ifndef ABSTRACTPROFILECONTAINER_HPP_
#define ABSTRACTPROFILECONTAINER_HPP_

#include <boost/shared_ptr.hpp>

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/profile/IProfileListener.hpp"
#include "kaa/profile/IProfileContainer.hpp"

namespace kaa {

/**
 * Container for the profile object which should be implemented by the user
 * @param <T> user-defined profile object.
 */
template<typename T>
class AbstractProfileContainer : public IProfileContainer {
public:
    AbstractProfileContainer() {}
    virtual ~AbstractProfileContainer() {}

    /**
     * Retrieves serialized profile.
     *
     * @return byte array with avro serialized profile.
     *
     */
    virtual SharedDataBuffer getSerializedProfile() {
        return avroConverter_.toByteArray(getProfile());
    }


    /**
     * Sets new profile listener.
     * DO NOT use this API explicitly.
     * @see IProfileContainer
     *
     * @param listener New profile listener.
     */
    virtual void setProfileListener(ProfileListenerPtr listener) {
        profileListener_ = listener;
    }

    /**
     * Retrieves user-defined profile object. Should be implemented by the user.
     *
     * @return profile object
     *
     */
    virtual T getProfile() = 0;

protected:

    /**
     * Updates profile. Call this method when you finish to update your profile.
     */
    void updateProfile() {
        if (profileListener_) {
            profileListener_->onProfileUpdated(getSerializedProfile());
        }
    }

private:
    AvroByteArrayConverter<T>   avroConverter_;
    ProfileListenerPtr          profileListener_;
};

} /* namespace kaa */

#endif /* ABSTRACTPROFILECONTAINER_HPP_ */
