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

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include <string>
#include <vector>
#include <memory>

namespace kaa {

/**
 * The listener of raw notifications' data.
 *
 * @author Denis Kimcherenko
 *
 */
class INotificationListener {
public:
    /**
     * Call on each received notification.
     *
     * @param topicId The topic's id to which notification is received.
     * @param notification The raw notification's data.
     *
     */
    virtual void onNotificationRaw(const std::string& topicId, const std::vector<std::uint8_t>& notification) = 0;

    virtual ~INotificationListener()
    {
    }
};

typedef std::shared_ptr<INotificationListener> INotificationListenerPtr;

} /* namespace kaa */

#endif

#endif /* INOTIFICATIONLISTENER_HPP_ */
