---
layout: page
title: Notifications
permalink: /:path/
sort_idx: 70
---

* [Basic architecture](#basic-architecture-2)
* [Configuring Kaa](#configuring-kaa-2)
  * [Notification topics](#notification-topics)
  * [Sending notifications](#sending-notifications)
* [Coding](#coding)
  * [Get available topics](#get-available-topics)
  * [Subscribe to updates on available topics](#subscribe-to-updates-on-available-topics)
  * [Default notification listener](#default-notification-listener)
  * [Topic specific notification listener](#topic-specific-notification-listener)
  * [Subscribe to optional topics](#subscribe-to-optional-topics)

The Kaa notification subsystem enables delivery of messages from the Kaa server to endpoints. The structure of the data that is carried by notifications is defined by the notification schema, which is configured on the Kaa server and built into Kaa endpoints. Please review the Kaa [notifications design reference]() for more details.

This guide will familiarize you with the basic concepts of Kaa notifications and programming of the Kaa notification subsystem. It is assumed that you have already set up either a [Kaa Sandbox]() or a [full-blown Kaa cluster]() and that you have created at least one [tenant]() and one [application]() in Kaa. We also recommend that you review [collecting endpoint profiles guide]() and [using endpoint groups]() before you proceed with this guide.

### Basic architecture
The following diagram illustrates basic entities and data flows in scope of the notification management:
Notifications are generated based on the notification schema created by the developer for the application 
The user or admin sends a notification using either Admin UI or REST API

The following diagram illustrates basic entities and data flows in scope of the notification management:
* Notifications are generated based on the [notification schema]() created by the developer for the application 
* The user or admin sends a notification using either [Admin UI]() or [REST API]()

![](images/basic_architecture_notification.png)

### Configuring Kaa
This section provides guidance on how to configure notifications in Kaa.

**Notification schema**


The default notification schema installed for Kaa applications is empty. You can configure your own notification schema using the Admin UI or REST API. For the purpose of this guide, we will use a simple notification schema shown in the following code block.

```json
{ 
    "type":"record",
    "name":"ExampleNotification",
    "namespace":"org.kaaproject.kaa.schema.sample.notification",
    "fields":[ 
        { 
            "name":"message",
            "type":"string"
        }
    ]
}
```

#### Notification topics


Notifications in Kaa are organized into topics. Each topic may be associated with one or more endpoint groups. To subscribe to a specific notification, endpoints must belong to one or more endpoint groups that are associated with the corresponding notification topic.
Topics can be mandatory or optional. Mandatory topic notifications are delivered in an enforced manner. Optional topics require subscription. It is responsibility of the client code to add notification listeners and subscribe to optional topics.
You can manage notification topics via [Admin UI]() or [REST API]().

***Once created, a notification topic does not impact any endpoints. To deliver notifications to some endpoint, at first you need to assign the topic to an endpoint group containing this endpoint via [Admin UI]() or [REST API]().***


Assuming that you have created custom endpoint groups from the [Using endpoint groups guide](), it would be logical to create and assign the following topics:

|                           | Endpoint Group Name |||||
|---------------------------|-----------------------|-------------------------|--------------------------|-----------------|----------------------------|
| Topic id                  | All                   | Android Froyo endpoints | Android endpoints        | iOS 8 endpoints | 3.0 RC1 QA group endpoints |
| Android notifications     | false                 | true                    | true                     | false           | false                      |
| iOS 8 notifications       | false                 | false                   | false                    | true            | false                      |
| All devices notifications | true                  | false                   | false                    | false           | false                      |


#### Sending notifications
To send a notification, you can issue the REST API request or use Admin UI.

### Coding
This section provides code samples which illustrate practical usage of notifications in Kaa.

#### Get available topics
To get a list of available topics, do the following:


<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-9">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-9">C++</a></li>
  <li><a data-toggle="tab" href="#C-9">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-9">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-9" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
...
    KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext())
    // Start Kaa client
    kaaClient.start()
...
 
    List<Topic> topics = kaaClient.getTopics();
 
    for (Topic topic : topics) {
        System.out.printf("Id: %s, name: %s, type: %s"
                , topic.getId(), topic.getName(), topic.getSubscriptionType());
    }
```

</div><div id="C_plus_plus-9" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/logging/LoggingUtils.hpp>
 
using namespace kaa;
 
...
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
// Start an endpoint
kaaClient->start();
 
// Get available topics
const auto& topics = kaaClient->getTopics();
for (const auto& topic : topics) {
    std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
}
```

</div><div id="C-9" class="tab-pane fade" markdown="1" >

```c
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_notification_manager.h>
 
kaa_client_t *kaa_client = /* ... */;
 
void on_topic_list_uploaded(void *context, kaa_list_t *topics)
{
    kaa_list_node_t *it = kaa_list_begin(topics);
    while (it) {
         kaa_topic_t *topic = (kaa_topic_t *) kaa_list_get_data(it);
         printf("Id: %lu, name: %s, type: %s\n", topic->id, topic->name, (topic->subscription_type == OPTIONAL_SUBSCRIPTION)? "OPTIONAL":"MANDATORY");  
         it = kaa_list_next(it);
    }
}
 
kaa_list_t *topics_list = NULL;
kaa_error_t error_core = kaa_get_topics(kaa_client_get_context(kaa_client)->notification_manager, &topics_list);
on_topic_list_uploaded(NULL, topics_list);
```

</div><div id="Objective-C-9" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
    id<KaaClient> kaaClient = [Kaa client]
...
    // Start Kaa client
    [kaaClient start]
...
 
    NSArray *topics = [kaaClient getTopics];
 
    for (Topic *topic in topics) {
        NSLog(@"%lld %@ %u", topic.id, topic.name, topic.subscriptionType);
    }
```

</div></div>

#### Subscribe to updates on available topics
To receive updates for the available topics list, add at least one listener as shown in the following code block (the number of listeners is not limited):

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-10">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-10">C++</a></li>
  <li><a data-toggle="tab" href="#C-10">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-10">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-10" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
...
// Add listener
kaaClient.addTopicListListener(new NotificationTopicListListener() {
    @Override
    public void onListUpdated(List<Topic> topics) {
        for (Topic topic : topics) {
            System.out.printf("Id: %s, name: %s, type: %s",
            topic.getId(), topic.getName(), topic.getSubscriptionType());
    }
}});
...
// Remove listener
kaaClient.removeTopicListListener(someOtherTopicUpdateListener);
```

</div><div id="C_plus_plus-10" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
#include <memory>
 
#include <kaa/Kaa.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/notification/INotificationTopicListListener.hpp>
 
using namespace kaa;
class NotificationTopicListListener : public INotificationTopicListListener {
public:
    virtual void onListUpdated(const Topics& topics)
    {
        for (const auto& topic : topics) {
            std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
        }
    }
};
...
// Create a listener which receives the list of available topics.
std::unique_ptr<NotificationTopicListListener> topicListListener(new NotificationTopicListListener());
 
// Add a listener
kaaClient->addTopicListListener(*topicListListener);
// Remove a listener
kaaClient->removeTopicListListener(*topicListListener);
```

</div><div id="C-10" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
kaa_topic_listener_t topic_listener = { &on_topic_list_uploaded, NULL };
uint32_t topic_listener_id = 0;
 
// Add listener
kaa_error_t error_code = kaa_add_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener, &topic_listener_id);
 
// Remove listener
error_code = kaa_remove_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener_id);
```

</div><div id="Objective-C-10" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController() <NotificationTopicListDelegate>
 
...
    // Add listener
    [self.kaaClient addTopicListDelegate:self];
...
 
- (void)onListUpdated:(NSArray *)list {
    for (Topic *topic in topics) {
        NSLog([NSString stringWithFormat:@"%lld %@ %u", topic.id, topic.name, topic.subscriptionType]);
    }
}
...
    // Remove listener
    [self.kaaClient removeTopicListDelegate:self];
```

</div></div>



When subscription changes simultaneously for several topics, the following approach is recommended for performance reasons:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-11">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-11">C++</a></li>
  <li><a data-toggle="tab" href="#C-11">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-11">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-11" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.notification.NotificationManager;
...
// Do subscription changes with parameter forceSync set to false
kaaClient.subscribeToTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), false);
kaaClient.unsubscribeFromTopic("boring_optional_topic_id", false);
...
// Add notification listener(s) (optional)
...
// Synchronizes new topic subscriptions.
kaaClient.syncTopicsList();
```

</div><div id="C_plus_plus-11" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
...
 
// Subscribe to the list of topics and unsubscribe from one topic.
// By setting the second parameter to false an endpoint postpones sending a subscription request till a user calls syncTopicSubscriptions().
 
kaaClient->subscribeOnTopics({"iOS 8 notifications", "another_optional_topic_id"}, false);
kaaClient->unsubscribeFromTopic("boring_optional_topic_id", false);
...
 
// Add notification listener(s) (optional)
 
...
// Sending a subscription request
kaaClient->syncTopicSubscriptions();
```

</div><div id="C-11" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
// Assume we have some optional topics
uint64_t topic_ids[] = { 12345, 6789 };
 
// Subscribe to the list of topics and unsubscribe from one topic.
// By setting the second parameter to false an endpoint postpones sending a subscription request till a user calls syncTopicSubscriptions().
kaa_error_t error_code = kaa_subscribe_to_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, sizeof(topic_ids) / sizeof(uint64_t), false);
error_code = kaa_unsubscribe_from_topic(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, false);
 
// Sending a subscription request
error_code = kaa_sync_topic_subscriptions(kaa_client_get_context(kaa_client)->notification_manager);
```

</div><div id="Objective-C-11" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
...
 
    // Do subscription changes with parameter forceSync set to false
    NSArray *topicIds = @[@"iOS 8 notifications", @"another_optional_topic_id"];
    [kaaClient subscribeToTopicsWithIDs:topicIds forceSync:NO];
    [kaaClient unsubscribeFromTopicWithId:@"boring_optional_topic_id" forceSync:NO];
    ...
     
    // Add notification listener(s) (optional)
    ...
     
    // Synchronizes new topic subscriptions.
    [kaaClient syncTopicsList];
```

</div></div>

#### Default notification listener
There are two types of topic notification listeners: the default and topic specific. To receive notifications, add at least one default listener (the number of default listeners is not limited) as shown in the following code block. As a result, the listener will receive notifications from all topics (mandatory topics, as well as optional topics having been subscribed to). 

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-12">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-12">C++</a></li>
  <li><a data-toggle="tab" href="#C-12">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-12">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-12" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.schema.sample.notification.ExampleNotification;
...
public class BasicNotificationListener implements NotificationListener {
 
    @Override
    public void onNotification(String topicId, ExampleNotification notification) {
        System.out.println("Received a notification: " + notification.toString());
    }
}
...
BasicNotificationListener listener = new BasicNotificationListener();
// Add listener
kaaClient.addNotificationListener(listener);
...
// Remove listener
kaaClient.removeNotificationListener(listener);
```

</div><div id="C_plus_plus-12" class="tab-pane fade" markdown="1" >

```c++
#include <cstdint>
#include <iostream>
#include <memory>
 
#include <kaa/notification/INotificationListener.hpp>
 
using namespace kaa;
 
class BasicNotificationListener : public INotificationListener {
public:
    virtual void onNotification(const std::int64_t topicId, const KaaNotification& notification)
    {
        std::cout << "Received notification on topic: id '"<< topicId << "', message: " << notification.message << std::endl;
    }
};
...
 
// Creates the listener which receives notifications on all available topics.
std::unique_ptr<BasicNotificationListener> listener(new BasicNotificationListener());
 
// Adds listener
kaaClient->addNotificationListener(*listener);
 
// Remove listener
kaaClient->removeNotificationListener(*listener);
```

</div><div id="C-12" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
{
    printf("Received notification on topic %llu: message='%s'\n", topic_id, notification->message);
}
 
kaa_notification_listener_t notification_listener = { &on_notification, NULL };
uint32_t notification_listener_id = 0;
 
// Add listener
kaa_error_t error_code = kaa_add_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &notification_listener, &notification_listener_id);
 
// Remove listener
error_code = kaa_remove_notification_listener(kaa_context_->notification_manager, &notification_listener_id);
```

</div><div id="Objective-C-12" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController () <NotificationDelegate>
 
...
 
 
- (void)onNotification:(KAASampleNotification *)notification withTopicId:(NSString *)topicId {
    NSLog(@"Received a notification: %@", notification);
}
 
    // Add listener
    [kaaClient addNotificationDelegate:self];
    ...
    // Remove listener
    [kaaClient removeNotificationDelegate:self];
```

</div></div>

#### Topic specific notification listener
To receive notifications on some specific topic (either mandatory or optional), you can use topic specific listeners (the number of listeners per topic is not limited) instead of the default listener. To create a topic specific listener, do the following: 

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-13">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-13">C++</a></li>
  <li><a data-toggle="tab" href="#C-13">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-13">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-13" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.schema.sample.notification.Notification;
...
BasicNotificationListener specificListener = new BasicNotificationListener();
// Add listener
kaaClient.addNotificationListener("All devices notifications", listener);
...
// Remove listener
kaaClient.removeNotificationListener("All devices notifications", listener);
```

</div><div id="C_plus_plus-13" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
#include <string>
#include <memory>
 
#include <kaa/Kaa.hpp>
 
...
 
// Create the listener which receives notifications on the specified optional topic.
std::unique_ptr<BasicNotificationListener> listener(new BasicNotificationListener());
 
// Add listener
kaaClient->addNotificationListener("All devices notifications", *listener);
 
// Remove listener
kaaClient->removeNotificationListener("All devices notifications", *listener);
```

</div><div id="C-13" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
 
// Assume we have some topic
uint64_t topic_id = 12345;
kaa_notification_listener_t notification_listener = { &on_notification, NULL };
uint32_t notification_listener_id = 0;
 
 
// Add listener
kaa_error_t error_code = kaa_add_optional_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &notification_listener, &topic_id, &notification_listener_id);
 
// Remove listener
error_code = kaa_remove_optional_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, &notification_listener_id);
```

</div><div id="Objective-C-13" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
 
// Add listener
[kaaClient addNotificationDelegate:self forTopicId:@"All devices notifications"];
...
// Remove listener
[kaaClient removeNotificationDelegate:self forTopicId:@"All devices notifications"];
```

</div></div>

#### Subscribe to optional topics
To receive notifications on some optional topic, at first subscribe to that topic as shown in the following code block:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-14">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-14">C++</a></li>
  <li><a data-toggle="tab" href="#C-14">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-14">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-14" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
...
// Add notification listener(s) (optional)
...
// Subscribe
kaaClient.subscribeToTopic("Android notifications", true);
...
// Unsubscribe. All added listeners will be removed automatically
kaaClient.unsubscribeFromTopic("Android notifications", true);
```

</div><div id="C_plus_plus-14" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
 
using namespace kaa;
 
...
 
// Add notification listener(s) (optional)
 
// Subscribe
kaaClient->subscribeToTopic("Android notifications");
 
// Unsubscribe
kaaClient->unsubscribeFromTopic("Android notifications");
```

</div><div id="C-14" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
// Assume we have some optional topic
uint64_t topic_id = 12345;
 
// Subcribe
kaa_error_t error_code = kaa_subscribe_to_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
 
// Unsubscribe. All added listeners will be removed automatically
error_code = kaa_unsubscribe_from_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
```

</div><div id="Objective-C-14" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
// Add notification listener(s) (optional)
 
// Subscribe
[kaaClient subscribeToTopicWithId:@"iOS notifications" forceSync:YES];
...
// Unsubscribe. All added listeners will be removed automatically
[kaaClient unsubscribeFromTopicWithId:@"iOS notifications" forceSync:YES];
```

</div></div>

You can work with a list of optional topics in a similar way as with a list of available topics.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-15">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-15">C++</a></li>
  <li><a data-toggle="tab" href="#C-15">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-15">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-15" class="tab-pane fade in active" markdown="1" >

```java
// Add notification listener(s) (optional)
...
// Subscribe
kaaClient.subscribeToTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), true);
...
// Unsubscribe
kaaClient.unsubscribeFromTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), true);
```

</div><div id="C_plus_plus-15" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
...
// Add notification listener(s) (optional)
 
// Subscribe
kaaClient->subscribeToTopics({"iOS 8 notifications", "another_optional_topic_id"});
 
// Unsubscribe
kaaClient->unsubscribeFromTopics({"iOS 8 notifications", "another_optional_topic_id"});
```

</div><div id="C-15" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
 
// Assume we have some optional topics
uint64_t topic_ids[] = { 12345, 6789 };
 
// Subscribe
kaa_error_t error_code = kaa_subscribe_to_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, sizeof(topic_ids) / sizeof(uint64_t), true);
 
// Unsubscribe
error_code = kaa_unsubscribe_from_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, topics_count, true);
```

</div><div id="Objective-C-15" class="tab-pane fade" markdown="1" >

```objective-c
// Add notification listener(s) (optional)
...
// Subscribe
[kaaClient subscribeToTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
...
// Unsubscribe
[kaaClient unsubscribeFromTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
```

</div></div>
