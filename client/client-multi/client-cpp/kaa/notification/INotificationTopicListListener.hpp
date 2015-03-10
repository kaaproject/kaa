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

#ifndef INOTIFICATIONTOPICLISTLISTENER_HPP_
#define INOTIFICATIONTOPICLISTLISTENER_HPP_

#include "kaa/KaaDefaults.hpp"

#include <memory>

namespace kaa {

typedef std::vector<Topic> Topics;

/**
 * <p>The listener to receive updates of available topics.</p>
 *
 * @author Denis Kimcherenko
 * @see INotificationManager
 *
 */
class INotificationTopicListListener {
public:
    /**
     * <p>Call on each updates of available topic list.</p>
     *
     * @param list The new list of available topics.
     * @see Topic
     */
    virtual void onListUpdated(const Topics& list) = 0;

    virtual ~INotificationTopicListListener() {}
};

typedef std::shared_ptr<INotificationTopicListListener> INotificationTopicListListenerPtr;

} /* namespace kaa */

#endif /* INOTIFICATIONTOPICLISTLISTENER_HPP_ */
