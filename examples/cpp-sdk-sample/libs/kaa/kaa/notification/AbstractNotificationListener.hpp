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

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/notification/INotificationListener.hpp"

namespace kaa {

/**
 * <p>Abstract listener to receive notifications.</p>
 *
 * <p>Responsible for processing notifications either for the specified topic
 * or for all together.</p>
 *
 * @code
 * // Assume, BasicNotification is a notification class auto-generated according to a predefined Avro schema
 *  class BasicNotificationListener : public AbstractNotificationListener<BasicNotification> {
 *      virtual void onNotification(const std::string& id, const BasicNotification& notification) {
 *          std::cout << "Received notification with body: " << notification.body << std::endl;
 *      }
 *  };
 * @endcode
 *
 * @author Denis Kimcherenko
 *
 */
template<typename T>
class AbstractNotificationListener : public INotificationListener {
public:
    /**
     * <p>Convert raw Avro-encoded data to a specific notification class according
     * to a predefined Avro schema.</p>
     *
     * @param topicId Unique topic identifier.
     * @param notification Raw Avro-encoded notification data.
     *
     */
    virtual void onNotificationRaw(const std::string& id, const std::vector<std::uint8_t>& notification) {
        onNotification(id, converter_.fromByteArray(notification.data(), notification.size()));
    }

    /**
     * <p>Call on each received notification.</p>
     *
     * @param topicId Unique topic identifier.
     * @param notification Received notification.
     *
     */
    virtual void onNotification(const std::string& id, const T& notification) = 0;

private:
    AvroByteArrayConverter<T>   converter_;
};

} /* namespace kaa */

#endif

#endif /* ABSTRACTNOTIFICATIONLISTENER_HPP_ */
