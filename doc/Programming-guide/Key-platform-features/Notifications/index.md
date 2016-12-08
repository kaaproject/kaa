---
layout: page
title: Notifications
permalink: /:path/
sort_idx: 70
---
{% include variables.md %}

* TOC
{:toc}


The Kaa **Notification subsystem** is used to deliver messages from [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) to [endpoints]({{root_url}}Glossary/#endpoint-ep).
Unlike configuration data that represents the desired endpoint state, notifications are used as calls for a dynamic endpoint action.
For example, a notification can cause a [Kaa client]({{root_url}}Glossary/#kaa-client) to display a UI message (a user notification).

## Prerequisites

To use the examples below, you need to first set up either a [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox) or a full-blown [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).
After that, you need to create a tenant with tenant admin, and an application.
To do this, you can use the server REST API ([tenant]({{root_url}}Programming-guide/Server-REST-APIs/#/Tenant), [tenant admin]({{root_url}}Programming-guide/Server-REST-APIs/#!/User/editUser), [application]({{root_url}}Programming-guide/Server-REST-APIs/#/Application)).

It is strongly recommended that you first read the [Data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection) and [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) sections before you proceed.

## Notification management

The following diagram illustrates the basic entities and data flows in scope of the notification management:

* Notifications are generated based on the [notification schema](#notification-schema) configured by the application developer.
* The user or administrator sends a notification using the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Notifications/sendNotification) call or the Administration UI (see [Send notifications](#send-notifications)).

![Basic architecture](images/basic_architecture_notification.png)

### Notification schema

The Kaa notifications functionality allows transferring any data to endpoints.
The structure of the notification data is defined by the notification schema selected and configured on the [Kaa server]({{root_url}}Glossary/#kaa-server).
The notification schema is defined similarly to the [endpoint profile]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) schema.
For more information about using schemas in Kaa, see [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library).

In addition to the [common type]({{root_url}}Glossary/#common-type-ct) schema and its version, notifications are characterized by type that can be either **system** or **user**.
System notifications are processed by the prepackaged endpoint functions, while user notifications are sent to the Kaa client using the the endpoint API.

Since the data structure requirements may evolve throughout the Kaa-based system lifetime, the Kaa server can be configured to simultaneously handle notification schemas of multiple versions.
In this case, a notification will have multiple schema versions associated with it, as well as multiple sets of notification data so that each schema version is covered.
To deliver such a notification to an endpoint, the server chooses the schema version supported by the endpoint.

The default notification schema installed for [Kaa applications]({{root_url}}Glossary/#kaa-application) is empty.

For the purpose of this guide, a simple notification schema is shown in the example below.

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

As a tenant developer, you can create new notification schemas for an application.
To do this, use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Notifications/saveNotificationSchema) or open the **Notification** page of the application and follow the same steps as described in [Setting client-side EP profile schema]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/#setting-client-side-ep-profile-schema).

![Add Notification Schema 1](images/add_notification_schema_1.png)

### Notification topics

Notifications in Kaa are organized into topics.
Every topic can be associated with one or more endpoint groups.
To receive a notification, the endpoint must belong to one or more groups that are associated with the corresponding notification topic.

Topics can be mandatory or optional.
Mandatory topic notifications are delivered to the endpoints by default, whereas optional topics require a subscription.
For more information, see [Subscribe to optional topics](#subscribe-to-optional-topics).

To edit a notification topic, use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Notifications/editTopic) or select the topic from the **Notification topics** page under the **Schema** section of the application.

![Add Notification Topic 1](images/add_notification_topic_1.png)

To create a notification topic, click the **Add notification topic** button.

![Add Notification Topic 2](images/add_notification_topic_2.png)

### Add notification topic to endpoint group

After you created a notification topic, you can assign it to the endpoint group.
To do this, use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Notifications/editTopic) or use the Administration UI:

1. Open the **Notification topics** page of the application, **Add notification topic**.

2. Enter the notification topic details and click **Add**.

3. Open the **Endpoint groups** page of the application and select the endpoint group from the list.

4. Under the **Notification topics** section, click the **Add notification topic** button, select the topic from the pop-up window and click **Add**.
Now all endpoints belonging to the selected group will be subscribed to notifications on these topics.

	![Add topic to endpoint group](images/add_topic_to_endpoint_group.png)

If you previously created custom endpoint groups as described in the [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#custom-endpoint-groups) section, the following table illustrates how you can assign notification topics to the endpoint groups.

<table>
    <tr>
        <th rowspan="2">Topic id</th>
        <th colspan="5"><center>Endpoint Group Name</center></th>
    </tr>
    <tr>
        <th>All</th>
        <th>Android Froyo endpoints</th>
        <th>Android endpoints</th>
        <th>iOS 8 endpoints</th>
        <th>3.0 RC1 QA group endpoints</th>
    </tr>
    <tr>
        <td>Android notifications</td>     
        <td>false</td> 
        <td>true</td>   
        <td>true</td>    
        <td>false</td>
        <td>false</td>      
    </tr>
    <tr>
        <td>iOS 8 notifications</td>      
        <td>false</td>                 
        <td>false</td>                   
        <td>false</td>                    
        <td>true</td>            
        <td>false</td>                      
    </tr>
    <tr>
        <td>All devices notifications</td> 
        <td>true</td>                 
        <td>false</td>                  
        <td>false</td>                    
        <td>false</td>           
        <td>false</td>                     
    </tr>
</table>

### Send notifications

To send a notification, use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Notifications/sendNotification) or use the Administration UI:

1. Select the topic from the **Notification topics** page of the application and click **Send notification**.

2. On the **Notification details** page, create a notification either by using the **Notification body** record form or by uploading a JSON file.
The data structure of your JSON file must match the corresponding notification schema.

	![Send Notification](images/send_notification.png)

3. Click **Send** to send the notification.

Below is an example of the uploaded file contents that will match the default Sandbox notification schema.

```
{"message": "Hello from Kaa!"}
```

A notification will be queued for delivery until the time you specified in the **Expires at** field of the **Notification details** page.
If you leave this field blank, the message will be queued until the default time-to-live (TTL) period expires.
For more information, see [Notification pipelines](#notification-pipelines).

If you specified an endpoint ID in the **Endpoint KeyHash** field, the notification will only be sent to that specific endpoint.
If you leave this field blank, the notification will be sent to all endpoints subscribed to the selected notification topic.

## Notification pipelines

Notifications are processed by Kaa server using the **notification pipelines**.

The server uses these pipelines to manage individual notifications within a topic.
A notification remains queued in the pipeline until its time-to-live (TTL) expires, after that the notification will be deleted.

Two types of pipelines are used depending on the scope of notifications delivery: **multicast** and **unicast** pipelines.
A multicast pipeline manages notifications for unlimited number of endpoints, while a unicast pipeline manages notifications for a single specific endpoint.

### Multicast pipelines

When you send a notification to a topic that more than one endpoint is subscribed to, that notification is added to a multicast pipeline with a unique sequential index.
Every endpoint maintains its position independently in the pipelines by storing the last sequential index received from each pipeline.

### Unicast pipelines

When you send a notification to a single endpoint by entering its KeyHash (see [Send notifications](#send-notifications)), that notification is added to a unicast pipeline with a unique ID.
Every endpoint handled by a unicast pipeline maintains its pipeline by reporting the received notification ID.
The server removes the notification from the pipeline once it receives a receipt confirmation from the endpoint.
Make sure that the endpoint you specified is subscribed to the corresponding notification topic, otherwise the notification will not be delivered.

## Notifications API

This section provides code samples to illustrate practical usage of notifications in Kaa.

### Get available topics

Below are code examples of how to get a list of available topics:

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
KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener(), true)
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
#include <kaa/IKaaClient.hpp>
#include <kaa/logging/LoggingUtils.hpp>
 
...
 
// Create an endpoint instance
auto kaaClient = kaa::Kaa::newClient();
// Start an endpoint
kaaClient->start();

// Get available topics
const auto& topics = kaaClient->getTopics();
for (const auto& topic : topics) {
    std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << kaa::LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
}
```

</div><div id="C-9" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
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

```objc
@import Kaa;
 
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

### Subscribe to optional topics

To receive notifications on optional topic, you need to subscribe to that topic.

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
 
...
 
// Subscribe
kaaClient->subscribeToTopic(1);
 
...
 
// Unsubscribe
kaaClient->unsubscribeFromTopic(1);
```

</div><div id="C-14" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
// Assume we have some optional topic
uint64_t topic_id = 12345;
 
// Subcribe
kaa_error_t error_code = kaa_subscribe_to_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
 
// Unsubscribe. All added listeners will be removed automatically
error_code = kaa_unsubscribe_from_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
```

</div><div id="Objective-C-14" class="tab-pane fade" markdown="1" >

```objc
@import Kaa;
 
...
// Add notification listener(s) (optional)
 
// Subscribe
[kaaClient subscribeToTopicWithId:@"iOS notifications" forceSync:YES];
...
// Unsubscribe. All added listeners will be removed automatically
[kaaClient unsubscribeFromTopicWithId:@"iOS notifications" forceSync:YES];
```

</div></div>

You can work with a list of optional topics in a similar way as with the list of available topics.

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
 
// Subscribe
kaaClient->subscribeToTopics({1, 2});
 
...
 
// Unsubscribe
kaaClient->unsubscribeFromTopics({1, 2});
```

</div><div id="C-15" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
 
// Assume we have some optional topics
uint64_t topic_ids[] = { 12345, 6789 };
 
// Subscribe
kaa_error_t error_code = kaa_subscribe_to_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, sizeof(topic_ids) / sizeof(uint64_t), true);
 
// Unsubscribe
error_code = kaa_unsubscribe_from_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, topics_count, true);
```

</div><div id="Objective-C-15" class="tab-pane fade" markdown="1" >

```objc
@import Kaa;
 
 ...
// Add notification listener(s) (optional)
 
// Subscribe
[kaaClient subscribeToTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
...
// Unsubscribe
[kaaClient unsubscribeFromTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
```

</div></div>


### Subscribe to updates on available topics

To receive updates for the available topics list, add at least one listener as shown in the example below.
You can create unlimited number of listeners.

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
 
class NotificationTopicListListener : public kaa::INotificationTopicListListener {
public:
    virtual void onListUpdated(const kaa::Topics& topics)
    {
        for (const auto& topic : topics) {
            std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << kaa::LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
        }
    }
};
 
...
 
// Create a listener which receives the list of available topics.
std::unique_ptr<NotificationTopicListListener> topicListListener(new NotificationTopicListListener());
 
// Add a listener
kaaClient->addTopicListListener(*topicListListener);
 
...
 
// Remove a listener
kaaClient->removeTopicListListener(*topicListListener);
```

</div><div id="C-10" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
kaa_topic_listener_t topic_listener = { &on_topic_list_uploaded, NULL };
uint32_t topic_listener_id = 0;
 
// Add listener
kaa_error_t error_code = kaa_add_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener, &topic_listener_id);
 
// Remove listener
error_code = kaa_remove_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener_id);
```

</div><div id="Objective-C-10" class="tab-pane fade" markdown="1" >

```objc
@import Kaa;
 
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

To accommodate for simultaneous change of several subscription topics, consider the following approach to optimize performance.

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
kaaClient->subscribeToTopics({1, 2}, false);
kaaClient->unsubscribeFromTopic(1, false);
 
...
 
// Sending a subscription request
kaaClient->syncTopicSubscriptions();
```

</div><div id="C-11" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
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

```objc
@import Kaa;
 
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

### Default notification listener

There are two types of topic notification listeners: default and topic-specific.
To receive notifications, add at least one default listener.
As a result, the listener will receive notifications from all topics (all mandatory and all optional topics) that the endpoint group is subscribed to.

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
#include <kaa/Kaa.hpp>
#include <kaa/notification/INotificationListener.hpp>
 
class BasicNotificationListener : public kaa::INotificationListener {
public:
    virtual void onNotification(const std::int64_t topicId, const kaa::KaaNotification& notification)
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
#include <extensions/notification/kaa_notification_manager.h>
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
error_code = kaa_remove_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &notification_listener_id);
```

</div><div id="Objective-C-12" class="tab-pane fade" markdown="1" >

```objc
@import Kaa;
 
@interface ViewController () <NotificationDelegate>
 
...
     // Add listener
    [kaaClient addNotificationDelegate:self];
...
 
- (void)onNotification:(KAASampleNotification *)notification withTopicId:(NSString *)topicId {
    NSLog(@"Received a notification: %@", notification);
}
 
...
    // Remove listener
    [kaaClient removeNotificationDelegate:self];
```

</div></div>

### Topic specific notification listener

To receive notifications on some specific topic (either mandatory or optional), you can use topic-specific listeners instead of the default listener.

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
#include <memory>
#include <kaa/Kaa.hpp>
 
...
 
// Create the listener which receives notifications on the specified optional topic.
std::unique_ptr<BasicNotificationListener> listener(new BasicNotificationListener());
 
// Add listener
kaaClient->addNotificationListener(1, *listener);
 
// Remove listener
kaaClient->removeNotificationListener(1, *listener);
```

</div><div id="C-13" class="tab-pane fade" markdown="1" >

```c
#include <extensions/notification/kaa_notification_manager.h>
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

```objc
@import Kaa;
 
...
// Add listener
[kaaClient addNotificationDelegate:self forTopicId:@"All devices notifications"];
...
// Remove listener
[kaaClient removeNotificationDelegate:self forTopicId:@"All devices notifications"];
```

</div></div>
