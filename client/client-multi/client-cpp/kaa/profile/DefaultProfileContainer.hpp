/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef CLIENT_CLIENT_MULTI_CLIENT_CPP_KAA_PROFILE_DEFAULTPROFILECONTAINER_HPP_
#define CLIENT_CLIENT_MULTI_CLIENT_CPP_KAA_PROFILE_DEFAULTPROFILECONTAINER_HPP_

#include <kaa/profile/AbstractProfileContainer.hpp>
#include <kaa/profile/gen/ProfileGen.hpp>
#include <kaa/profile/gen/ProfileDefinitions.hpp>

using namespace kaa;

class DefaultProfileContainer : public AbstractProfileContainer< KaaProfile > {
public:
    DefaultProfileContainer() : profile_(KaaProfile()) { }
    DefaultProfileContainer(const KaaProfile& profile) : profile_(profile) { }

    virtual KaaProfile getProfile()
    {
        return profile_;
    }
private:
    KaaProfile profile_;
};

#endif /* CLIENT_CLIENT_MULTI_CLIENT_CPP_KAA_PROFILE_DEFAULTPROFILECONTAINER_HPP_ */
