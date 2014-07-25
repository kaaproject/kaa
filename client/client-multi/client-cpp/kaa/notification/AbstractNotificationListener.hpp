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

#ifndef ABSTRACTNOTIFICATIONLISTENER_HPP_
#define ABSTRACTNOTIFICATIONLISTENER_HPP_

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/notification/INotificationListener.hpp"

namespace kaa {

/**
 * Implements @link INotificationListener @endlink and ables to convert the raw notification's data to
 * the needed object.
 *
 * @author Yaroslav Zeygerman
 *
 */
template<typename T>
class AbstractNotificationListener : public INotificationListener {
public:
    /**
     * Will be called when topic notification is received
     *
     * @param id topic's id for which a notification is received
     * @param notification body of the topic notification
     *
     */
    virtual void onNotificationRaw(const std::string& id, const std::vector<boost::uint8_t>& notification) {
        onNotification(id, converter_.fromByteArray(notification.data(), notification.size()));
    }

    /**
     * User-defined routine for notification processing
     * @param id topic's id for which a notification is received
     * @param notification avro-specific body of the topic notification
     */
    virtual void onNotification(const std::string& id, const T& notification) = 0;

private:
    AvroByteArrayConverter<T>   converter_;
};

} /* namespace kaa */

#endif /* ABSTRACTNOTIFICATIONLISTENER_HPP_ */
