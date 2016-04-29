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

#ifndef DEFAULTPROFILECONTAINER_HPP_
#define DEFAULTPROFILECONTAINER_HPP_

#include <kaa/profile/IProfileContainer.hpp>
#include <kaa/profile/gen/ProfileDefinitions.hpp>

namespace kaa {

class DefaultProfileContainer : public IProfileContainer {
public:
    DefaultProfileContainer() : profile_(KaaProfile()) { }
    DefaultProfileContainer(const KaaProfile& profile) : profile_(profile) { }

    KaaProfile getProfile()
    {
        return profile_;
    }

    void setProfile(const KaaProfile& profile)
    {
        profile_= profile;
    }

private:
    KaaProfile profile_;
};

}

#endif /* DEFAULTPROFILECONTAINER_HPP_ */
