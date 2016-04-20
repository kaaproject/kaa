---
layout: page
title: Getting started
permalink: /:path/
nav: /:path/Programming-guide/Getting-started/
sort_idx: 10
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Kaa sandbox](#kaa-sandbox)
  - [Video tutorial](#video-tutorial)
  - [Installation](#installation)
    - [System requirements](#system-requirements)
    - [Installation steps](#installation-steps)
    - [Know issues](#know-issues)
    - [Troubleshooting](#troubleshooting)
  - [Configuration](#configuration)
    - [Outgoing mail settings](#outgoing-mail-settings)
    - [Networking](#networking)
  - [Kaa Sandbox web UI](#kaa-sandbox-web-ui)
    - [Demo projects](#demo-projects)
    - [Admin UI](#admin-ui)
    - [Avro UI](#avro-ui)
- [Your first Kaa application](#your-first-kaa-application)
  - [Adding application](#adding-application)
  - [Create notification schema](#create-notification-schema)
  - [Generate SDK](#generate-sdk)
  - [Sample client application](#sample-client-application)
  - [Create notification topic](#create-notification-topic)
  - [Create notification](#create-notification)
- [Next Steps](#next-steps)
- [Further reading](#further-reading)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This section provides guidance on how to create your first Kaa application that will work with the Kaa platform. In this guide we will show you how to create a simple desktop java application that will receive notifications from the Kaa server and display them on the console. We will define our own notification schema and use the generated java classes within our application.

# Kaa Sandbox

Kaa Sandbox is a private Kaa environment which includes demo client applications. Sandbox includes all necessary Kaa components in a convenient virtual environment that can be set up in just 5 minutes!
With the use of Kaa Sandbox, anyone can learn Kaa, build a proof of concept and test their own applications locally.

## Video tutorial

<iframe width="800" height="500"
src="https://www.youtube.com/embed/ynbxcRdgXFU">
</iframe>

## Installation Sandbox

Kaa Sandbox is presented as a stand-alone virtual machine.

### System requirements

To use Kaa Sandbox, your system must meet the following minimum system requirements.

- 64-bit OS
- 4GB RAM
- Virtualization enabled in BIOS

### Installation steps

To install Kaa Sandbox, perform the following steps:

1. Install the virtualization environment.
The current version of Kaa Sandbox supports [Oracle VirtualBox 4.2+](https://www.virtualbox.org/wiki/Downloads) which is available as a free download.

2. Download the Sandbox image from [Kaa download page](http://www.kaaproject.org/download-kaa/).

3. Import the Sandbox image using this [guide](https://www.virtualbox.org/manual/ch01.html#ovf).

### Known issues

Please take into account the following known issues and limitations of Kaa Sandbox.

* Without the SMTP server configured, you will not be able to create new users. See the [Outgoing mail settings section](#outgoing-mail-settings) for more details.


### Troubleshooting

Common issues covered in this [guide]({{root_url}}Administration-guide/Troubleshooting/).

## Configuration

### Outgoing mail settings

Outgoing mail settings are used to send emails to newly created users with the information about their passwords, as well as other notifications.
By default, outgoing mail settings are not configured for Admin UI. To target Admin UI to your SMTP server refer to the [Admin UI guide***]({{root_url}}Administration-guide/Tenants-and-applications-management).

### Networking

By default, Kaa Sandbox components are accessible from a host machine only. But if you want to share Kaa Sandbox in the local network you need to reconfigure the network interface for this virtual machine in [Bridge mode](https://www.virtualbox.org/manual/ch06.html#network_bridged). Once the virtual box is available to devices on your local/test network, you need to change Sandbox host/IP on [web UI](#kaa-sandbox-web-ui) or execute the script on Sandbox.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Sandbox-web-ui">Sandbox web UI</a></li>
  <li><a data-toggle="tab" href="#Console">Console</a></li>
</ul>

<div class="tab-content">

<div id="Sandbox-web-ui" class="tab-pane fade in active" markdown="1">

Go to Kaa Sandbox web UI and in a upper right corner select "Management" menu item. Input new host/IP and click the "Update" button in the "Kaa host/IP" section.

<img src="attach/managment_tab.png" width="800" height="500">

</div><div id="Console" class="tab-pane fade" markdown="1">

```sh
sudo /usr/lib/kaa-sandbox/bin/change_kaa_host.sh <new host/ip>
```

</div></div>

## Kaa Sandbox web UI

Kaa Sandbox web UI provides you with access to Kaa demo projects and some basic Kaa Sandbox configuration capabilities. Once Kaa Sandbox is installed and opened, the web UI is available at the following URL (by default): [http://127.0.0.1:9080/sandbox](http://127.0.0.1:9080/sandbox).

### Demo projects

You can download both source and binary distributions for each demo project. A downloaded binary already contains Kaa SDK that targets current Kaa Sandbox. Thus, if you successfully configure the [networking](#networking) for your SDK, the downloaded application will be able to access it and will work correctly.

<img src="attach/Sandbox.png" width="800" height="400">

## Admin UI

You can access Admin UI by clicking **Administrative console** at the top of the window.
Refer to the [Admin UI guide]({{root_url}}Administration-guide/Tenants-and-applications-management) for working instructions.
**NOTE**
Kaa Sandbox provides default credentials for all three types of Kaa users, as follows:
* Kaa admin - kaa/kaa123
* Tenant admin - admin/admin123
* Tenant developer - devuser/devuser123

## Avro UI

You can access Avro UI by clicking **Avro UI sandbox console** at the top of the window.
Refer to the [Avro UI guide]({{root_url}}Administration-guide/Tenants-and-applications-management)  for working instructions.

# Your first Kaa application

To register a new application within a fresh Kaa server installation, you need to create users with the _tenant administrator_ and _tenant developer_ roles. The tenant administrator is responsible for creating new applications in Kaa, and the tenant developer configures and generates SDKs for those applications. We suggest that you use Kaa Sandbox, which has a tenant administrator and tenant developer users already created.

## Add application

To add an application, proceed as follows:

1. Open the Kaa admin UI in your browser ( [http://127.0.0.1:8080](http://127.0.0.1:8080) ) and log in as a tenant administrator (user/password: admin/admin123).
2. Select **Applications** on the navigation panel on the left side and, in the **Applications** window that opens, click **Add new application.**
3. In the **Add new application** window, fill in the fields as required and then click **Add**.  
![](attach/image2015-6-15 20-2-55.png)

After the application has been added, you may log out. We will not be using the tenant administrator role in this guide anymore.

## Create notification schema

The application that you have created in the previous step already includes the default versions of the profile, configuration, notification and log schemas ready for use. However, in this sample application, we will use a custom notification schema for demonstration purposes. To create and upload the schema, proceed as follows:

1. Create the _schema.json_ file on your PC with the following schema definition:

   ```json
   {
     "type": "record",
     "name": "Notification",
     "namespace": "org.kaaproject.kaa.schema.example",
     "fields":[
        {
          "name": "message",
          "type": "string"
        }
      ]
    }
```

2. Open the admin UI in your browser ( [http://127.0.0.1:8080](http://127.0.0.1:8080/) ) and log in as a tenant developer (user/password: devuser/devuser123).
3. Open the relevant **Notification schemas** window (**Applications =\> My First Kaa Application =\> Schemas =\> Notification**) and click **Add new schema**.
4. In the **Add new schema** window, fill in the fields as shown in the following screenshot and then click **Add**.   
<img src="attach/image2015-6-15 20-9-14.png" width="800" height="400">

5. Scroll down and use the Upload from file function to find the previously created json file with the schema. Alternatively, you can use the Schema [Avro UI form***]() to create the schema.
6. Click Upload.  
<img src="attach/image2015-6-19 14-51-26.png" width="900" height="500">
7. Click Add at the top of the window.

As a result of this operation you will see two notification schemas in the list:
<img src="attach/image2015-6-15 20-18-54.png" width="1200" height="300">

In this screenshot, version 2.0 is the notification schema that was just created. We will use this version for the SDK generation in the next step.

## Generate SDK

To generate the SDK for the new application, proceed as follows:

1. Select the **My First Kaa Application** application and click **Generate SDK**.   
<img src="attach/sdk_gen0.png" width="494" height="430">

2. Click **Add SDK profile**.   
<img src="attach/sdk_gen1.png" width="700" height="210">

3. In the **Add SDK profile** window, fill in the fields as shown in the following screenshot  and then click **Add**.   
<img src="attach/sdk_gen2.png" width="500" height="500">

4. Click **Generate SDK** for corresponding SDK profile. In the **Generate SDK window** select the target platform for your SDK and click **Generate SDK**.  
<img src="attach/sdk_gen3.png" width="350" height="350">

After the SDK is generated, you will be presented with a window asking you to save a .jar file with the generated SDK. Specify the file name and location on your computer and then click **Save**. The SDK is now downloaded to your computer.

Please note that we are generating an SDK based on the default configuration, profile, and log schemas. These schemas are automatically populated during the application's creation. If necessary, you can overwrite them using the [Admin UI]({{root_url}}Administration-guide/Tenants-and-applications-management).

## Sample client application

Once you have downloaded the SDK, you can use it in your sample project. The following code block illustrates a simple desktop java application that will receive notifications from the Kaa server and display them on the console.

**NOTE**: After generating the C/C++/Objective-C SDKs, you need to build them before creating the application.

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

</div><div id="Cpp" class="tab-pane fade" markdown="1">

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

    void on_notification(void \*context, uint64_t \*topic_id, kaa_notification_t \*notification)
    {
        (void)context;
        kaa_string_t \*message = (kaa_string_t \*)notification->message;
        printf("Notification for topic id '%lu' received\n", \*topic_id);
        printf("Notification body: %s\n", message->data);
    }

    void on_topics_received(void \*context, kaa_list_t \*topics)
    {
        printf("Topic list was updated\n");
        if (!topics || !kaa_list_get_size(topics)) {
            printf("Topic list is empty");
            return;
        }

        kaa_list_node_t \*it = kaa_list_begin(topics);
        while (it) {
            kaa_topic_t \*topic = (kaa_topic_t \*)kaa_list_get_data(it);
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
    - (void)onListUpdated:(NSArray \*)list {
      NSLog(@"%@ Topic list updated!", TAG);

      if ([list count] == 0) {
          NSLog(@"%@ Topic list is empty!", TAG);
          return;
      }

      for (Topic \*topic in list) {
        NSLog(@"%@ Received topic with id %lld and name %@", TAG, topic.id, topic.name);
      }
    }

    - (KAAEmptyData \*)getProfile {
        return [[KAAEmptyData alloc] init];
      }

    - (void)onNotification:(KAANotification \*)notification withTopicId:(int64_t)topicId {
      NSLog(@"%@ Received notification %@ for topic with id %lld", TAG, notification.message, topicId);
    }

    - (void)onStarted {
      NSLog(@"%@ Kaa SDK client started", TAG);
    }
    @end
```
</div></div>

You can find the project source code (including Java, C, C++ and Objective-C) in the attached [archive](attach/kaa-first-app.zip).

- **For Java**, the project is built using Apache Maven. Note that the downloaded SDK must be placed into the lib folder in order for the build to work. Import this project into your IDE as a Maven project and launch the _NotificationSystemTestApp_ application. Once the application is launched, you will see the following output:  

    [...  INFO  NotificationSystemTestApp]  Kaa SDK client started!

- **For C, C++ and Objective-C**, place the downloaded SDK into the _libs_ folder and then run _build.sh_ or _build.bat_ script depending on your platform. Launch the _demo_client_ application. Once the application is launched, you will see the following output:  

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
<img src="attach/image2015-6-19 14-51-26.png" width="800" height="500">
3. In the **Send notification** window, fill in the fields as shown in the following screenshot and then click **Send**.  
![](attach/image2014-7-28 13-52-48.png)

Once the notification is sent, you can see the following output in the application:

    [...  INFO  NotificationSystemTestApp]  Received notification {"message": "Hello from Kaa!"} for topic with id X

**Congratulations with your first Kaa application!**

# Next steps
To create a real-world IoT solution, you will most likely need to implement more features into your application. Kaa provides you with practically everything you might need. The following overview will help you grasp the scope of Kaa capabilities as well as get familiar with the essential documentation, such as [Programming guide]({{root_url}}Programming-guide) and [Administration UI]({{root_url}}Administration-guide/Tenants-and-applications-management) guide.

**Profiling and grouping**
During a new endpoint registration, Kaa creates an associated _endpoint profile_ for the endpoint. An endpoint profile is basically some meaningful information about the endpoint which may be useful for specific applications. Profiles may contain things like an OS version, amount of RAM, average battery life, type of network connection, device operation mode – virtually anything. An endpoint profile structure in Kaa is configured using a client-side endpoint profile schema. Based on the defined profile schema, Kaa generates an object model to operate against the client side and handles data marshaling all the way to the database. Whenever the client updates its profile information, the endpoint SDK automatically sends these updates to the server as soon as the connection becomes available.

For programming practice, see [collecting endpoint profiles]({{root_url}}Programming-guide/Key-system-features/Endpoint-profiling/).

The information collected in an endpoint’s profile can be used to group endpoints into independently managed entities called _endpoint groups_. On the back end, Kaa provides a [profile filtering language***]() for defining the criteria for group membership. An endpoint can belong to any number of groups. Grouping endpoints can be used, for example, to send targeted notifications or adjust software behavior by applying group-specific configuration overrides.

For programming practice, see [using endpoint groups]({{root_url}}Programming-guide/Key-system-features/Endpoint-groups-management/).

**Events**
Kaa allows for delivery of _events_, which are structured messages, across endpoints. When endpoints register with the Kaa server, they communicate which event types they are able to generate and receive. Kaa allows endpoints to send events either to virtual “chat rooms” or to individual endpoints. Events can even be delivered across applications registered with Kaa – making it possible to quickly integrate and enable interoperability between endpoints running different applications. Some examples are: a mobile application that controls house lighting, a car’s GPS that communicates with the home security system, a set of integrated audio systems from different vendors that deliver a smooth playback experience as you walk from one room to another. Kaa events are implemented in a generic, abstract way, using non-proprietary schema definitions that ensure identical message structures. The schema provides independence from any specific functionality implementation details.
For programming practice, see [messaging across endpoints]().

**Collecting data**
Kaa provides rich capabilities for collecting and storing structured data from endpoints. A typical use-case is collecting various types of logs: performance, user behavior, exceptional conditions, etc.

Using a set of pre-packaged server-side _log appenders_, the Kaa server is able to store records to a filesystem, a variety of big data platforms (Hadoop, MongoDB, Cassandra, Oracle NoSQL etc.), or submit them directly to a streaming analytics system. It is also possible to [create a custom log appender***]().

The structure of the collected data is flexible and defined by the [log schema***](). Based on the log schema defined for the Kaa application, Kaa generates an object model for the records and the corresponding API calls in the client SDK. Kaa also takes care of data marshalling, managing temporary data storage on the endpoint, and uploading data to the Kaa server.

For programming practice, see [collecting data from endpoints***]().

**Using notifications**
Kaa uses _notifications_ to distribute structured messages, posted within _notification topics_, from the server to endpoints. A notification structure is defined by a corresponding [notification schema***]().

Endpoint are subscribed to notification topics, which can be either mandatory or optional. Access to notification topics is automatically granted according to the endpoint’s group membership. Notifications can be sent either to every endpoint subscribed to a topic or to an individual endpoint.

Notifications can be assigned expiration timestamps to prevent their delivery after a certain period of time.

For programming practice, see [using notifications***]().

**Distributing operational data**
Kaa allows you to perform operational data updates, such as configuration data updates, from the Kaa server to endpoints. This feature can be used for centralized configuration management, content distribution, etc. Since Kaa works with structured data and constraint types, it guarantees data integrity.

The Kaa server monitors the database for changes and distributes updates to endpoints in the incremental form, thus ensuring efficient bandwidth use. The endpoint SDK performs data merging and persistence, as well as notifies the client code about the specific changes made to the data. As a result, the client application knows exactly where in the data structure the changes occurred and can be programmed to react accordingly.

Based on the endpoint’s group membership, it is possible to control what data is available to the endpoint. This is achieved by applying group-specific data overrides, which make it possible to adjust the behavior of the client application based on operational conditions or usage patterns, fine-tune the algorithms according to feedback, implement gradual feature roll-out, A/B testing, etc.
For programming practice, see [distributing data to endpoints***]().

# Further reading

Use the following guides and references to make the most of Kaa.

| Guide                                                          | What it is for                                                                                                                                                                                           |
|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Key system features]({{root_url}}Programming-guide/Key-system-features/)                           | Use this reference to learn about features and capabilities of Kaa \([Endpoint profiling]({{root_url}}Programming-guide/Key-system-features/Endpoint-profiling/), [Events\*\*\*](#), [Notifications\*\*\*](#), [Logging\*\*\*](#), and other features\). |
| [Installation guide]({{root_url}}Administration-guide/System-installation)                       | Use this guide to install and configure Kaa either on a single Linux node or in a cluster environment.                                                                                                    |
| [Contribute To Kaa]({{root_url}}Customization-guide/How-to-contribute/)                       | Use this guide to learn how to contribute to Kaa project and which code/documentation style conventions we adhere to.                                                                                                   |
