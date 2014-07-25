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

#ifndef INOTIFICATIONTOPICSLISTENER_HPP_
#define INOTIFICATIONTOPICSLISTENER_HPP_

#include <list>
#include <string>

namespace kaa {

typedef std::vector<Topic> Topics;

/**
 *  Interface for listeners of topic's name list update
 */
class INotificationTopicsListener {
public:
    /**
     *  Will be called when new topic's name list are received
     *  @param newList comprises of names of new topics
     */
    virtual void onListUpdated(const Topics& newList) = 0;

    virtual ~INotificationTopicsListener() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONTOPICSLISTENER_HPP_ */
