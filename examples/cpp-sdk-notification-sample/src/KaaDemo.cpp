/*
 * Copyright 2014-2015 CyberVision, Inc.
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


#include <memory>
#include <unordered_map>

#include <kaa/Kaa.hpp>
#include <kaa/logging/Log.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/notification/INotificationTopicListListener.hpp>

using namespace kaa;

/*
 * Send the notification with this body to stop the demo application.
 */
#define KAA_SHUTDOWN_MESSAGE    "shutdown"

/*
 * Switches to true, when the demo application receives the notification with the message body is set to KAA_SHUTDOWN_MESSAGE.
 */
bool_type isShutdown = false;

/*
 * The container for listeners which receives notification on the specific optional topic.
 */
std::unordered_map<std::string, std::shared_ptr<INotificationListener>> optionalTopicListeners;

/*
 * The listener which receives notifications on topics.
 */
class CommonNotificationListener : public INotificationListener
{
public:
    virtual void onNotification(const std::string& topicId, const KaaNotification& notification)
    {
        KAA_LOG_TRACE(boost::format("Received notification on topic '%1%': message='%2%'")
                                                                % topicId % notification.message);
        checkShutdown(notification.message);
    }

protected:
    void checkShutdown(const std::string& message)
    {
        if (message.compare(KAA_SHUTDOWN_MESSAGE) == 0) {
            KAA_LOG_INFO("Shutdown message received!");
            isShutdown = true;
        }
    }
};

/*
 * The listener which receives notifications on topics.
 */
class OptionalNotificationListener : public CommonNotificationListener
{
public:
    virtual void onNotification(const std::string& topicId, const KaaNotification& notification)
    {
        KAA_LOG_TRACE(boost::format("Received notification on optional topic '%1%': message='%2%'")
                                                                    % topicId % notification.message);
        checkShutdown(notification.message);
    }
};

/*
 * The listener which receives the list of available topics.
 */
class NotificationTopicListListener : public INotificationTopicListListener
{
public:
    NotificationTopicListListener(IKaaClient& kaaClient) : kaaClient_(kaaClient) {}

    virtual void onListUpdated(const Topics& topics)
    {
        KAA_LOG_INFO(boost::format("%1% new topic(s) received") % topics.size());

        std::size_t subscriptionCount = 0;

        for (const auto& topic : topics) {
            KAA_LOG_TRACE(boost::format("Topic: id '%1%', name '%2%', type '%3%'")
                % topic.id % topic.name % LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType));

            /*
             * Adds listener for every new optional topic.
             */
            if (topic.subscriptionType == SubscriptionType::OPTIONAL) {
                auto it = optionalTopicListeners.find(topic.id);
                if (it == optionalTopicListeners.end()) {
                    ++subscriptionCount;

                    /*
                     * Creates the listener which receives notifications on the specified optional topic.
                     */
                    std::shared_ptr<CommonNotificationListener> listener(new OptionalNotificationListener);
                    optionalTopicListeners.insert(std::make_pair(topic.id, listener));

                    KAA_LOG_TRACE(boost::format("Going to subscribe to optional topic '%1%'") % topic.id);

                    /*
                     * Subscribes to the specified optional topic.
                     * Adds listener which will receive notification on this topic.
                     */
                    kaaClient_.subscribeToTopic(topic.id, false);
                    kaaClient_.addNotificationListener(topic.id, *listener);
                }
            }
        }

        /*
         * Synchronizes new topic subscriptions.
         */
        if (subscriptionCount > 0) {
            kaaClient_.syncTopicSubscriptions();
        }
    }

private:
    IKaaClient&    kaaClient_;
};

int main()
{
    /*
     * Initializes the Kaa endpoint.
     */
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();

    /*
     * Creates the listener which receives the list of available topics.
     */
    std::unique_ptr<NotificationTopicListListener> topicListListener(new NotificationTopicListListener(kaaClient));

    /*
     * Creates the listener which receives notifications on all available topics.
     */
    std::unique_ptr<CommonNotificationListener> commonNotificationListener(new CommonNotificationListener);

    /*
     * Adds listeners
     */
    kaaClient.addTopicListListener(*topicListListener);
    kaaClient.addNotificationListener(*commonNotificationListener);

    /*
     * Runs the Kaa endpoint.
     */
    Kaa::start();

    /*
     * Waits until the shutdown message is received (see KAA_SHUTDOWN_MESSAGE).
     * To receive this message, send the notification with the KAA_SHUTDOWN_MESSAGE body to the demo application.
     */
    while (!isShutdown) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    /*
     * Stops the Kaa endpoint.
     */
    Kaa::stop();

    return 0;
}
