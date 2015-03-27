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

#include <kaa/Kaa.hpp>
#include <kaa/logging/Log.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/notification/INotificationTopicListListener.hpp>
#include <kaa/common/exception/UnavailableTopicException.hpp>

using namespace kaa;


static void showTopicList(const Topics& topics)
{
    if (topics.empty()) {
        std::cout << "Topic list is empty" << std::endl;
    } else {
        for (const auto& topic : topics) {
            std::cout << (boost::format("Topic: id '%1%', name '%2%', type '%3%'")
                % topic.id % topic.name % LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType)) << std::endl;
        }
    }
}

static std::list<std::string> extractOptionalTopicIds(const Topics& topics) {
    std::list<std::string> topicIds;
    for (const auto& topic : topics) {
        if (topic.subscriptionType == SubscriptionType::OPTIONAL) {
            topicIds.push_back(topic.id);
        }
    }
    return topicIds;
}


// The listener which receives notifications on topics.
class BasicNotificationListener : public INotificationListener {
public:
    virtual void onNotification(const std::string& topicId, const KaaNotification& notification)
    {
        std::cout << (boost::format("Notification for topic id '%1%' received") % topicId) << std::endl;
        std::cout << (boost::format("Notification body: '%1%'")
            % (notification.message.is_null() ? "" : notification.message.get_string())) << std::endl;
    }
};

// A listener that tracks the notification topic list updates
// and subscribes the Kaa client to every new topic available.
class BasicNotificationTopicListListener : public INotificationTopicListListener {
public:
    BasicNotificationTopicListListener(IKaaClient& kaaClient) : kaaClient_(kaaClient) {}

    virtual void onListUpdated(const Topics& topics)
    {
        std::cout << ("Topic list was updated") << std::endl;
        showTopicList(topics);

        try {
            auto optionalTopicIds = extractOptionalTopicIds(topics);
            for (const auto& id : optionalTopicIds) {
                std::cout << (boost::format("Subscribing to optional topic '%1%'") % id) << std::endl;
            }

            kaaClient_.subscribeToTopics(optionalTopicIds);
        } catch (UnavailableTopicException& e) {
            std::cout << (boost::format("Topic is unavailable, can't subscribe: %1%") % e.what()) << std::endl;
        }
    }

private:
    IKaaClient&    kaaClient_;
};

int main()
{
    std::cout << "Notification demo started" << std::endl;
    std::cout << "--= Press Enter to exit =--" << std::endl;

    // Initialize the Kaa endpoint.
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();

    // Add the listener which receives the list of available topics.
    std::unique_ptr<INotificationTopicListListener> topicListListener(new BasicNotificationTopicListListener(kaaClient));
    kaaClient.addTopicListListener(*topicListListener);

    // Add the listener which receives notifications on all topics.
    std::unique_ptr<INotificationListener> commonNotificationListener(new BasicNotificationListener);
    kaaClient.addNotificationListener(*commonNotificationListener);

    // Start the Kaa client and connect it to the Kaa server.
    Kaa::start();

    // Get available notification topics.
    auto availableTopics = kaaClient.getTopics();

    // List the obtained notification topics.
    showTopicList(availableTopics);

    std::cin.get();

    // Remove the listener which receives the list of available topics.
    kaaClient.removeTopicListListener(*topicListListener);

    // Stop the Kaa client and release all the resources which were in use.
    Kaa::stop();
    std::cout << "Notification demo stopped" << std::endl;

    return 0;
}
