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

#ifndef INOTIFICATIONLISTENER_HPP_
#define INOTIFICATIONLISTENER_HPP_

#include <string>
#include <cstdint>

#include "kaa/notification/gen/NotificationDefinitions.hpp"

namespace kaa {

/**
 * @brief The listener which receives notifications on the specified topic.
 *
 * @author Denis Kimcherenko
 *
 */
class INotificationListener {
public:
    /**
     * @brief Callback is used when the new notification on the specified topic is received.
     *
     * @param[in] topicId         The id of the topic on which the notification is received.
     * @param[in] notification    The notification data.
     *
     */
    virtual void onNotification(const std::int64_t topicId, const KaaNotification& notification) = 0;

    virtual ~INotificationListener() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONLISTENER_HPP_ */
