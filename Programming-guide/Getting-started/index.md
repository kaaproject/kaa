---
layout: page
title: Getting started
permalink: /:path/
nav: /:path/Programming-guide/Getting-started/
sort_idx: 10
---

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Installation and configuration](#installation-and-configuration)
- [Your first Kaa application](#your-first-kaa-application)
  - [Add application](#add-application)
  - [Create notification schema](#create-notification-schema)
  - [Generate SDK](#generate-sdk)
  - [Sample client application](#sample-client-application)
  - [Create notification topic](#create-notification-topic)
  - [Create notification](#create-notification)
- [Further reading](#further-reading)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This section provides guidance on how to create your first Kaa application that will work with the Kaa platform. In this guide we will show you how to create a simple desktop java application that will receive notifications from the Kaa server and display them on the console. We will define our own notification schema and use the generated java classes within our application.

# Installation and configuration

Before you start using the Kaa framework, you need to install it. You can install Kaa in the [single node](Single-node-installation) mode or [distributed ](../../Administration-guide/System-installation/Cluster-setup)mode. The installation procedure is described in [Installation guide](../../Administration-guide/System-installation).

However, we recommend that you start exploring Kaa using Sandbox. Kaa Sandbox is an easy-to-use virtual environment that includes all the components that you need in order to learn Kaa, build a proof of concept and test your own applications locally. Sandbox also includes demo client applications.

The Sandbox setup procedure is straightforward and can be done instantly. See [Sandbox guide](Sandbox) for details.

# Your first Kaa application

To register a new application within a fresh Kaa server installation, you need to create users with the _tenant administrator_ and _tenant developer_ roles. The tenant administrator is responsible for creating new applications in Kaa, and the tenant developer configures and generates SDKs for those applications. We suggest that you use Kaa Sandbox, which has a tenant administrator and tenant developer users already created.

## Adding application

To add an application, proceed as follows:

1. Open the Kaa admin UI in your browser ( [http://127.0.0.1:8080](http://127.0.0.1:8080) ) and log in as a tenant administrator (user/password: admin/admin123).
2. Select **Applications** on the navigation panel on the left side and, in the **Applications** window that opens, click **Add application.**
3. In the **Add application** window, fill in the fields as required and then click **Add**.  
![](attach/image2015-6-15 20-2-55.png)

After the application has been added, you may log out. We will not be using the tenant administrator role in this guide anymore.

## Create notification schema

The application that you have created in the previous step already includes the default versions of the profile, configuration, notification and log schemas ready for use. However, in this sample application, we will use a custom notification schema for demonstration purposes. To create and upload the schema, proceed as follows:

* Create the _schema.json_ file on your PC with the following schema definition:      

```json
{
  "type": "record",
  "name": "Notification",
  "namespace": "org.kaaproject.kaa.schema.example",
  "fields": [
    {
      "name": "message",
      "type": "string"
    }
  ]
}
```

* Open the admin UI in your browser ( [http://127.0.0.1:8080](http://127.0.0.1:8080/) ) and log in as a tenant developer (user/password: devuser/devuser123).
* Open the relevant **Notification schemas** window (**Applications =\> My First Kaa Application =\> Schemas =\> Notification**) and click **Add new schema**.
* In the **Add notification schema** window, enter the name and description of the new notification schema.
![](attach/image2015-6-15 20-9-14.png)

* Scroll down and use the **Upload from file** function to find the previously created json file with the schema. Alternatively, you can use the Schema [Avro UI form]() to create the schema.
* Click **Upload**.                                                                
![](attach/image2015-6-19 14-51-26.png)

* Click **Add** at the top of the window.
As a result of this operation you will see two notification schemas in the list:
![](attach/image2015-6-15 20-18-54.png)

In this screenshot, version 2.0 is the notification schema that was just created. We will use this version for the SDK generation in the next step.

## Generate SDK

To generate the SDK for the new application, proceed as follows:

1. Select the **My First Kaa Application** application and click **Generate SDK.**
2. In the **Generate SDK** window, fill in the fields as shown in the following screenshot (for the target platform you can use either Java, C, C++ or Objective-C) and then click **Generate SDK**.
![](attach/image2014-7-28 13-21-3.png)

After the SDK is generated, you will be presented with a window asking you to save a .jar file with the generated SDK (for Java) or an archive with the generated SDK (for C, C++ or Objective-C). Specify the file name and location on your computer and then click **Save**. The SDK is now downloaded to your computer.

Note that in this example we are generating the SDK based on the default configuration, profile, and log schemas. These schemas are automatically populated during the creation of the application. If necessary, you can overwrite them using [Admin UI](Admin-UI).

## Sample client application

Once you have downloaded the SDK, you can use it in your sample project. The following code block illustrates a simple desktop java application that will receive notifications from the Kaa server and display them on the console.

**NOTE**: After generating the C/C++/Objective-C SDKs, you need to [build them]() before creating the application.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#Cpp">C++</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objectve-C">Objectve-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1">
```Java
    package org.kaaproject.kaa.samples.nf;

    import java.util.List;

    import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
    import org.kaaproject.kaa.client.Kaa;
    import org.kaaproject.kaa.client.KaaClient;
    import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
    import org.kaaproject.kaa.client.notification.NotificationListener;
    import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
    import org.kaaproject.kaa.common.endpoint.gen.Topic;
    import org.kaaproject.kaa.schema.example.Notification;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    public class NotificationSystemTestApp {

        private static final Logger LOG = LoggerFactory.getLogger(NotificationSystemTestApp.class);

        public static void main(String[] args) {
            new NotificationSystemTestApp().launch();
        }

        private void launch() {
            // Create client for Kaa SDK
            KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(),
                    new SimpleKaaClientStateListener() {
                        @Override
                        public void onStarted() {
                            LOG.info("Kaa SDK client started!");
                        }
                    });

            // Registering listener for topic updates
            kaaClient.addTopicListListener(new NotificationTopicListListener() {
                @Override
                public void onListUpdated(List<Topic> topicList) {
                    LOG.info("Topic list updated!");
                    for (Topic topic : topicList) {
                        LOG.info("Received topic with id {} and name {}", topic.getId(), topic.getName());
                    }
                }
            });

            // Registering listener for notifications
            kaaClient.addNotificationListener(new NotificationListener() {
                @Override
                public void onNotification(String topicId, Notification notification) {
                    LOG.info("Received notification {} for topic with id {}", notification, topicId);
                }
            });

            // Starts Kaa SDK client
            kaaClient.start();
        }
    }
```
</div><div id="Cpp" class="tab-pane fade in active" markdown="1">
```C
    #include <cstdint>
    #include <iostream>
    #include <memory>

    #include <kaa/Kaa.hpp>
    #include <kaa/notification/INotificationListener.hpp>
    #include <kaa/notification/INotificationTopicListListener.hpp>

    using namespace kaa;

    class SimpleKaaClientStateListener : public IKaaClientStateListener {
    public:
        virtual void onStarted() {
            std::cout << "Kaa SDK client started!" << std::endl;
        }

        virtual void onStartFailure(const KaaException& exception) {}
        virtual void onPaused()  {}
        virtual void onPauseFailure(const KaaException& exception) {}
        virtual void onResumed() {}
        virtual void onResumeFailure(const KaaException& exception) {}

        virtual void onStopped() {}
        virtual void onStopFailure(const KaaException& exception) {}
    };

    class BasicNotificationTopicListListener : public INotificationTopicListListener {
    public:
        virtual void onListUpdated(const Topics& topics)
        {
            std::cout << "Topic list was updated" << std::endl;
            for (const auto& topic : topics) {
            std::cout << "Received topic with id " << topic.id << " and name '" << topic.name << "'" << std::endl;
        }
        }
    };

    class BasicNotificationListener : public INotificationListener {
    public:
        virtual void onNotification(const std::int64_t topicId, const KaaNotification& notification)
        {
            std::cout << "Received notification '" << notification.message << "'"
                      << "for topic with id '" << topicId  << "'" << std::endl;
        }
    };

    int main()
    {
        BasicNotificationTopicListListener topicListListener;
        BasicNotificationListener commonNotificationListener;
        // Create client for Kaa SDK
        auto kaaClient = Kaa::newClient(std::make_shared<KaaClientPlatformContext>(),
                                    std::make_shared<SimpleKaaClientStateListener>());
        kaaClient->addTopicListListener(topicListListener);
        kaaClient->addNotificationListener(commonNotificationListener);
        // Start Kaa SDK client
        kaaClient->start();

        std::cout << "Presss any key to stop Kaa SDK client" << std::endl;
        std::getchar();

        // Stop Kaa SDK client
        kaaClient->stop();

        return 0;
    }
```
</div><div id="C" class="tab-pane fade" markdown="1">
```C
    #include <stdint.h>
    #include <stdio.h>
    #include <string.h>
    #include <time.h>
    #include <kaa/kaa_error.h>
    #include <kaa/platform/kaa_client.h>
    #include <kaa/utilities/kaa_log.h>
    #include <kaa/kaa_notification_manager.h>

    #define KAA_DEMO_RETURN_IF_ERROR(error, message) \
        if ((error)) { \
            printf(message ", error code %d\n", (error)); \
            return (error); \
        }

    static kaa_client_t *kaa_client = NULL;

    void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
    {
        (void)context;
        kaa_string_t *message = (kaa_string_t *)notification->message;
        printf("Notification for topic id '%lu' received\n", *topic_id);
        printf("Notification body: %s\n", message->data);
    }

    void on_topics_received(void *context, kaa_list_t *topics)
    {
        printf("Topic list was updated\n");
        if (!topics || !kaa_list_get_size(topics)) {
            printf("Topic list is empty");
            return;
        }

        kaa_list_node_t *it = kaa_list_begin(topics);
        while (it) {
            kaa_topic_t *topic = (kaa_topic_t *)kaa_list_get_data(it);
            printf("Topic: id '%lu', name: %s\n", topic->id, topic->name);
            it = kaa_list_next(it);
        }
    }

    int main()
    {
        printf("Kaa SDK client started\n");

        kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
        KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

        kaa_topic_listener_t topic_listener = { &on_topics_received, kaa_client };
        kaa_notification_listener_t notification_listener = { &on_notification, kaa_client };

        uint32_t topic_listener_id = 0;
        uint32_t notification_listener_id = 0;

        error_code = kaa_add_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager
                                           , &topic_listener
                                           , &topic_listener_id);
        KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed add topic listener");

        error_code = kaa_add_notification_listener(kaa_client_get_context(kaa_client)->notification_manager
                                             , &notification_listener
                                             , &notification_listener_id);
        KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed add notification listener");

        error_code = kaa_client_start(kaa_client, NULL, NULL, 0);
        KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

        kaa_client_destroy(kaa_client);

        return error_code;
    }
```
</div><div id="Objectve-C" class="tab-pane fade" markdown="1">
```Objective-C
    #import "ViewController.h"
    #import <Kaa/Kaa.h>

    #define TAG @"NotificationSystemTestApp >>>"

    @interface ViewController () <KaaClientStateDelegate, NotificationTopicListDelegate, NotificationDelegate, ProfileContainer>

    @property (nonatomic, strong) id<KaaClient> kaaClient;

    @end


    @implementation ViewController

    - (void)viewDidLoad {
        [super viewDidLoad];
        //Create a Kaa client with default Kaa context.
        self.kaaClient = [Kaa clientWithContext:[[DefaultKaaPlatformContext alloc] init]];

        // A listener that listens to the notification topic list updates.
        [self.kaaClient addTopicListDelegate:self];

        // Add a notification listener that listens to all notifications.
        [self.kaaClient addNotificationDelegate:self];

        // Set up profile container, needed for ProfileManager
        [self.kaaClient setProfileContainer:self];

        // Start the Kaa client and connect it to the Kaa server.
        [self.kaaClient start];

    }
    - (void)onListUpdated:(NSArray *)list {
        NSLog(@"%@ Topic list updated!", TAG);

        if ([list count] == 0) {
            NSLog(@"%@ Topic list is empty!", TAG);
            return;
        }

        for (Topic *topic in list) {
            NSLog(@"%@ Received topic with id %lld and name %@", TAG, topic.id, topic.name);
        }
    }

    - (KAAEmptyData *)getProfile {
        return [[KAAEmptyData alloc] init];
    }

    - (void)onNotification:(KAANotification *)notification withTopicId:(int64_t)topicId {
        NSLog(@"%@ Received notification %@ for topic with id %lld", TAG, notification.message, topicId);
    }

    - (void)onStarted {
        NSLog(@"%@ Kaa SDK client started", TAG);
    }
    @end
```
</div>
</div> <!-- Tab content -->
You can find the project source code in the attached [archive](attach/kaa-first-app.zip).


* **For Java**, the project is built using Apache Maven. Note that the downloaded SDK must be placed into the *lib* folder in order for the build to work. Import this project into your IDE as a Maven project and launch the *NotificationSystemTestApp* application. Once the application is launched, you will see the following output:

    [...  INFO  NotificationSystemTestApp]  Kaa SDK client started!

* **For C, C++ and Objective-C**, place the downloaded SDK into the *libs* folder and then run *build.sh* or *build.bat* script depending on your platform. Launch the *demo_client* application. Once the application is launched, you will see the following output:

    Kaa SDK client started!

## Create notification topic

To send your first Kaa notification, you need to create a notification topic and assign this topic to the default endpoint group.

To create a notification topic, proceed as follows:

1. Open the relevant **Notification topics** window (**Applications =\> My First Kaa Application =\> Notification topics**) and click **Add new notification topic**.
2. In the **Add notification topic** window, fill in the fields as shown in the following screenshot and then click **Add**.  
![](attach/image2014-7-28 13-38-3.png)

**NOTE:** We set the topic as mandatory in order to automatically subscribe the client application to notifications on this topic.

Once the topic is created, we will assign it to the default endpoint group, which contains all endpoints, including endpoints with our application.

To assign a notification topic to the default endpoint group, proceed as follows:

1. In the relevant **Endpoint groups** window (**Applications=\>My First Kaa Application=\>Endpoint groups**), select the _All_ group.
2. In the **Endpoint group details** window, click **Add notification topic** at the bottom of the window.
3. In the **Add topic to endpoint group** window, select the recently created notification topic and then click **Add**.  
![](attach/image2014-7-28 13-43-38.png)

After the topic is added to the endpoint group, you will see the following output in the application:

    [...  INFO  NotificationSystemTestApp]  Topic list updated!
    [...  INFO  NotificationSystemTestApp]  Received topic with id X and name Notification Topic

This is the first update from the Kaa server that provides the client with the information about the changes in the topic list. Now you can send notifications on this topic, and they will be delivered to your application.

## Create notification

To create a notification, proceed as follows:

1. Create a _notification.json_ file on your PC with the following contents:

    {"message":"Hello from Kaa!"}

2. Open the relevant **Notification topics** window (**Applications =\> My First Kaa Application =\> Notification topics**) and click **Send notification** in the **Notification Topic** row.  
![](attach/image2015-6-15 20-21-36.png)
3. In the **Send notification** window, fill in the fields as shown in the following screenshot and then click **Send**.  
![](attach/image2014-7-28 13-52-48.png)

Once the notification is sent, you can see the following output in the application:

    [...  INFO  NotificationSystemTestApp]  Received notification {"message": "Hello from Kaa!"} for topic with id X

**Congratulations with your first Kaa application!**

## Next steps
To create a real-world IoT solution, you will most likely need to implement more features into your application. Kaa provides you with practically everything you might need. The following overview will help you grasp the scope of Kaa capabilities as well as get familiar with the essential documentation, such as [Programming guide]() and [Administration UI]() guide.

**Profiling and grouping**
During a new endpoint registration, Kaa creates an associated _endpoint profile_ for the endpoint. An endpoint profile is basically some meaningful information about the endpoint which may be useful for specific applications. Profiles may contain things like an OS version, amount of RAM, average battery life, type of network connection, device operation mode – virtually anything. An endpoint profile structure in Kaa is configured using a client-side endpoint [profile schema](). Based on the defined profile schema, Kaa generates an object model to operate against the client side and handles data marshaling all the way to the database. Whenever the client updates its profile information, the endpoint SDK automatically sends these updates to the server as soon as the connection becomes available.

For programming practice, see [collecting endpoint profiles]().

The information collected in an endpoint’s profile can be used to group endpoints into independently managed entities called _endpoint groups_. On the back end, Kaa provides a [profile filtering language]() for defining the criteria for group membership. An endpoint can belong to any number of groups. Grouping endpoints can be used, for example, to send targeted notifications or adjust software behavior by applying group-specific configuration overrides.

For programming practice, see [using endpoint groups]().

**Events**
Kaa allows for delivery of _events_, which are structured messages, across endpoints. When endpoints register with the Kaa server, they communicate which event types they are able to generate and receive. Kaa allows endpoints to send events either to virtual “chat rooms” or to individual endpoints. Events can even be delivered across applications registered with Kaa – making it possible to quickly integrate and enable interoperability between endpoints running different applications. Some examples are: a mobile application that controls house lighting, a car’s GPS that communicates with the home security system, a set of integrated audio systems from different vendors that deliver a smooth playback experience as you walk from one room to another. Kaa events are implemented in a generic, abstract way, using non-proprietary schema definitions that ensure identical message structures. The schema provides independence from any specific functionality implementation details.
For programming practice, see [messaging across endpoints]().

**Collecting data**
Kaa provides rich capabilities for collecting and storing structured data from endpoints. A typical use-case is collecting various types of logs: performance, user behavior, exceptional conditions, etc.

Using a set of pre-packaged server-side _log appenders_, the Kaa server is able to store records to a filesystem, a variety of big data platforms (Hadoop, MongoDB, Cassandra, Oracle NoSQL etc.), or submit them directly to a streaming analytics system. It is also possible to [create a custom log appender]().

The structure of the collected data is flexible and defined by the [log schema](). Based on the log schema defined for the Kaa application, Kaa generates an object model for the records and the corresponding API calls in the client SDK. Kaa also takes care of data marshalling, managing temporary data storage on the endpoint, and uploading data to the Kaa server.

For programming practice, see [collecting data from endpoints]().

**Using notifications**
Kaa uses _notifications_ to distribute structured messages, posted within _notification topics_, from the server to endpoints. A notification structure is defined by a corresponding [notification schema]().

Endpoint are subscribed to notification topics, which can be either mandatory or optional. Access to notification topics is automatically granted according to the endpoint’s group membership. Notifications can be sent either to every endpoint subscribed to a topic or to an individual endpoint.

Notifications can be assigned expiration timestamps to prevent their delivery after a certain period of time.

For programming practice, see [using notifications]().

**Distributing operational data**
Kaa allows you to perform operational data updates, such as configuration data updates, from the Kaa server to endpoints. This feature can be used for centralized configuration management, content distribution, etc. Since Kaa works with structured data and constraint types, it guarantees data integrity.

The Kaa server monitors the database for changes and distributes updates to endpoints in the incremental form, thus ensuring efficient bandwidth use. The endpoint SDK performs data merging and persistence, as well as notifies the client code about the specific changes made to the data. As a result, the client application knows exactly where in the data structure the changes occurred and can be programmed to react accordingly.

Based on the endpoint’s group membership, it is possible to control what data is available to the endpoint. This is achieved by applying group-specific data overrides, which make it possible to adjust the behavior of the client application based on operational conditions or usage patterns, fine-tune the algorithms according to feedback, implement gradual feature roll-out, A/B testing, etc.
For programming practice, see [distributing data to endpoints]().

# Further reading

Use the following guides and references to make the most of Kaa.

| Guide                                                          | What it is for                                                                                                                                                                                           |
|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Design reference](Design-reference)                           | Use this reference to learn about features and capabilities of Kaa ([Endpoint profiling](Endpoint-profiling), [Events](Events), [Notifications](Notifications), [Logging](Logging), and other features). |
| [Sandbox](Sandbox)                                             | Use this guide to try out Kaa in a private environment with demo applications.                                                                                                                           |
| [Development environment setup](Development-environment-setup) | Use this guide to set up necessary environment for installing and programming Kaa.                                                                                                                       |
| [Installation guide](Installation-guide)                       | Use this guide to install and configure Kaa either on a single Linux node or in a cluster environment.                                                                                                    |
