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

#ifndef INOTIFICATIONTOPICLISTLISTENER_HPP_
#define INOTIFICATIONTOPICLISTLISTENER_HPP_

#include "kaa/notification/gen/NotificationDefinitions.hpp"

namespace kaa {

/**
 * @brief The listener which receives updates on available topics.
 *
 * @author Denis Kimcherenko
 *
 */
class INotificationTopicListListener {
public:
    /**
     * @brief Callback is used when the new list of available topics is received.
     *
     * @param[in] topics    The new list of available topics.
     * @see Topics
     */
    virtual void onListUpdated(const Topics& topics) = 0;

    virtual ~INotificationTopicListListener() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONTOPICLISTLISTENER_HPP_ */
