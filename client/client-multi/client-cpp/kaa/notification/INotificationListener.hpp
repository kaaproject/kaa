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

#ifndef INOTIFICATIONLISTENER_HPP_
#define INOTIFICATIONLISTENER_HPP_

#include <string>
#include <vector>

#include <boost/cstdint.hpp>

namespace kaa {

/**
 * Interface for listeners of topic notifications
 */
class INotificationListener {
public:
    /**
     * Will be called when topic notification is received
     * @param topicId id of the topic for which a notification is received
     * @param notification body of the topic notification
     */
    virtual void onNotificationRaw(const std::string& topicId
                                 , const std::vector<boost::uint8_t>& notification) = 0;

    virtual ~INotificationListener() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONLISTENER_HPP_ */
