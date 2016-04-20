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

#ifndef INOTIFICATIONPROCESSOR_HPP_
#define INOTIFICATIONPROCESSOR_HPP_

#include <vector>

#include "kaa/notification/gen/NotificationDefinitions.hpp"
#include "kaa/notification/INotificationManager.hpp"

namespace kaa {

/**
 *  Interface for listeners of both topic and notification list updates
 */
class INotificationProcessor {
public:
    /**
     *  Will be called when new topic list are received
     *  @param topics comprises of new topics
     */
    virtual void topicsListUpdated(const Topics& topics) = 0;

    /**
     *  Will be called when new topic list are received
     *  @param notifications comprises of new notifications
     */
    virtual void notificationReceived(const Notifications& notifications) = 0;

    virtual ~INotificationProcessor() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONPROCESSOR_HPP_ */
