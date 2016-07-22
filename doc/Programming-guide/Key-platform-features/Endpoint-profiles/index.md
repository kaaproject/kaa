---
layout: page
title: Endpoint profiles
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

The structure of both client-side and server-side endpoint profile is a customizable structured data set that describes specific characteristics of the endpoint. Endpoint profiles are used to classify endpoints into 
endpoint groups and are comprised of the client-side, server-side and system part. The structure of both client-side and server-side of endpoint profile is defined by 
application developer using the [Apache Avro schema](http://avro.apache.org/docs/current/spec.html#schemas) format. Application developer may reuse and share certain data 
structures using CTL.  
Client-side structure is used during SDK generation. Thus, change to the client-side structure requires re-generation of the SDK 
(for more details see [Endpoint SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs)). Application developer is able to define and 
change server-side structure of endpoint profile at any time. The structure of the system part is identical across the applications and is used by Kaa internally for its 
functions. Both client-side and server-side profile schemas are maintained within the corresponding application, with its own version that distinguishes it from the previous 
schemas. Multiple schema versions and corresponding endpoint profiles created upon those schemas can coexist within a single application.
  
## Client-side endpoint profile ##

The client-side endpoint profile is initially generated at the stage of a new endpoint registration. Prior to that, the client-side endpoint profile values should be 
specified by the client developer using the Kaa endpoint SDK. The client-side endpoint profile can be also updated at run time using SDK API call. In this case, SDK executes 
profile update request and endpoint membership in the endpoint groups is re-evaluated and updated to match the new endpoint profile.
The client-side endpoint profile is unidirectionally synchronized, and thus should not be considered as a means to temporarily store endpoint data in the Kaa cluster. 
There is no way for the endpoint to retrieve the profile information back from the Kaa cluster. At its start, the application must fill in the current endpoint profile 
with the up-to-date data and execute corresponding SDK API calls. The endpoint SDK does not persist the profile information over the endpoint reboots. However, 
it detects profile data changes and submits the new data to the Kaa cluster as a profile update.

<img src="endpoint-profile-generation/ClientSideEndpointProfileGeneration_0_8_0.png">

### Client-side endpoint profile example ###

For the purpose of this guide we will use a fairly abstract client-side profile schema shown below.

The schema defines 4 fields:

* id - unique identifier
* os - operating system name
* os_version - version of operating system
* build - build version

```json
{
    "type":"record",
    "name":"Profile",
    "namespace":"org.kaaproject.kaa.schema.sample.profile",
    "fields":[
        {
            "name":"id",
            "type":"string"
        },
        {
            "name":"os",
            "type":{
                "type":"enum",
                "name":"OS",
                "symbols":[
                    "Android",
                    "iOS",
                    "Linux"
                ]
            }
        },
        {
            "name":"os_version",
            "type":"string"
        },
        {
            "name":"build",
            "type":"string"
        }
    ]
}
```

The following client-side profile is based on the rules that sets schema.

```json
{
  "id" : "1",
  "os" : "Android",
  "os_version" : "1",
  "build" : "1"
}
```
The schema structure from our example allows filtering the endpoints by the operation systems of device (for example, to show only Android devices), os_version (for example, to push some 
notifications only for the specified os version). It is allowed to create complex filtering conditions by combining as many filtering conditions as needed.

### Setting client-side endpoint profile schema from Admin UI

The list of client-side endpoint profile schemas created by a tenant developer for the application is shown in the **Client-side EP profile** schemas window, which can 
be opened from the application menu on the navigation panel as illustrated by the following screenshot.

<img src="admin-ui/Client-side endpoint profile schema.png">

To export the client-side EP profile schema, click **Export** in the last column of desired schema row and select export method from drop-down. 
See [CT schema export support]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library/#ct-schema-export-support) for available export methods details.
As a tenant developer, you can create new client-side EP schemas for the application as follows:

1. In the **Client-side EP profile schemas** window for the application, click **Add schema**.
2. In the **Add profile schema** window enter the name of the schema.
3. Then create a schema using one of the two options:
    * Using the existing CT by clicking **Select existing type** and selecting exiting CT version from FQN and version drop-downs.
    
    <img src="admin-ui/Create client-side endpoint profile schema 1.png">
    
    * Create new CT by clicking **Create new type**. In this case you will be redirected to **Add new type** window. Here you can create a schema either by using 
    the schema form or by uploading a schema in the [Avro](http://avro.apache.org/docs/current/spec.html) format from a file.
    
    <img src="admin-ui/Create client-side endpoint profile schema 2.png">
    
4. Click **Add** at the top of the window to save the schema.

If you want to review the added Avro schema, open the corresponding **Client-side EP profile schema** window by clicking the schema in the **Client-side EP profile schemas** window.

<img src="admin-ui/View client-side endpoint profile schema.png">

### REST API for Client-side endpoint profile

Visit [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO) documentation page for detailed description of the REST API, its purpose, interfaces and features supported.


### Client side endpoint profile SDK API ###

Endpoint profile information changes as the result of the client operation or user's actions, it is the client implementation responsibility to update the 
profile via the endpoint SDK API calls. The endpoint SDK detects profile changes by comparing the new profile hash against the previously persisted one. Should there be a change, 
the endpoint profile management module passes it to the Operations service, which in turn updates the endpoint profile information in the database and revises the endpoint 
groups membership.

Think about the client-side profile schema as of a structured data set of your endpoint application that will later be available to you in Kaa server and may change 
due to your client application logic or device state.
You can configure your own client-side profile schema using the 
[Admin UI](#setting-client-side-endpoint-profile-schema-from-admin-ui) or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO). 
Client-side endpoint profile updates are reported to the endpoint SDK using a profile container. The profile related API varies depending on the target SDK platform, 
however the general approach is the same.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus">C++</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.schema.sample.profile.OS;
import org.kaaproject.kaa.schema.sample.profile.Profile;
 
// Profile is an auto-generated class based on user defined schema.
Profile profile;
// Desktop Kaa client initialization
public void init() {
    // Create instance of desktop Kaa client for Kaa SDK
    KaaClient client = Kaa.newClient(new DesktopKaaPlatformContext(),
            new SimpleKaaClientStateListener());
    // Sample profile
    profile = new Profile("id", OS.Linux, "3.17", "0.0.1-SNAPSHOT");
    // Simple implementation of ProfileContainer interface that is provided by the SDK
    client.setProfileContainer(new ProfileContainer() {
        @Override
        public Profile getProfile() {
            return profile;
        }
    });
    // Starts Kaa
    client.start();
    // Update to profile variable
    profile.setBuild("0.0.1-SNAPSHOT");
    // Report update to Kaa SDK. Force delivery of updated profile to server.
    client.updateProfile();
}
```

</div>

<div id="C_plus_plus" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
 
#include <kaa/Kaa.hpp>
#include <kaa\profile\DefaultProfileContainer.hpp>
 
using namespace kaa;
 
...
 
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
// Create an endpoint's profile
Profile profile;
profile.id = "deviceId";
profile.os_version = "3.17";
profile.os = OS::Linux;
profile.build = "0.0.1-SNAPSHOT";
 
// Set a profile container to pass a profile to an endpoint
kaaClient->setProfileContainer(std::make_shared<DefaultProfileContainer>(profile));
 
// Call each time when a profile is updated
kaaClient->updateProfile();
 
// Start an endpoint
kaaClient->start();
```

</div>

<div id="C" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_profile.h>
#include <kaa/gen/kaa_profile_gen.h> // auto-generated header
 
#define KAA_EXAMPLE_PROFILE_ID "sampleid"
#define KAA_EXAMPLE_OS_VERSION "1.0"
#define KAA_EXAMPLE_BUILD_INFO "3cbaf67e"
 
kaa_client_t *kaa_client = /* ... */;
 
/* Assume Kaa SDK is already initialized */
 
/* Create and update profile */
kaa_profile_t *profile = kaa_profile_profile_create();
 
profile->id = kaa_string_move_create(KAA_EXAMPLE_PROFILE_ID, NULL);
profile->os = ENUM_OS_Linux;
profile->os_version = kaa_string_move_create(KAA_EXAMPLE_OS_VERSION, NULL);
profile->build = kaa_string_move_create(KAA_EXAMPLE_BUILD_INFO, NULL);
 
kaa_error_t error_code = kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);
 
/* Check error code */
 
profile->destroy(profile);

```
</div>

<div id="Objective-C" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
#import "ViewController.h"
 
@interface ViewController () <ProfileContainer>
 
@property (nonatomic, strong) KAAProfile *profile;
 
@end
 
@implementation ViewController
- (void)initClient() {
    // Create instance of Kaa client
    id<KaaClient> client = [Kaa client];
    // Sample profile that is an auto-generated class based on user defined schema.
    _profile = [[KAAProfile alloc] initWithId:@"id" os:OS_LINUX code:@"3.17" build:@"0.0.1-SNAPSHOT"];
    // Simple implementation of ProfileContainer interface that is provided by the SDK
    [client setProfileContainer:self];
    // Starts Kaa
    [client start];
    // Update to profile variable
    _profile.build = @"0.0.1-SNAPSHOT";
    // Report update to Kaa SDK. Force delivery of updated profile to server.
    [client updateProfile];
}
 
- (KAAProfile *)getProfile {
    return _profile;
}
 
...
 
@end

```
</div>
</div>

## Server-side endpoint profile ##

The server-side endpoint profile is initially generated at the stage of a new endpoint registration. By default, server side profile record is auto-generated based on 
the latest server-side profile schema of particular application. Both server-side endpoint profile schema and data can be updated at run time using 
[Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO). In this case, endpoint membership in the endpoint groups is re-evaluated and updated to match the new endpoint profile.

<img src="endpoint-profile-generation/ServerSideEndpointProfileGeneration_0_8_0.png">

### Server-side endpoint profile example ###

The following code block provides a simple server-side profile schema example.

```json
{  
   "type":"record",
   "name":"ServerProfile",
   "namespace":"org.kaaproject.kaa.schema.sample.profile",
   "fields":[  
      {  
         "name":"subscriptionPlan",
         "type":"string"
      },
      {  
         "name":"activationFlag",
         "type":"boolean"
      }
   ]
}
```
The following server-side profile would be compatible with our schema example.

```json
{
    "subscriptionPlan": "plan",
    "activationFlag": "false"
}
```

The schema structure from our example allows filtering the endpoints by the serial number (SN) and customer ID. For example, you may remotely turn on/off features 
for certain customers or deactivate device with certain SN. It is allowed to create complex filtering conditions by combining as many filtering conditions as needed.

### Server-side endpoint profile update ###

Think about the server-side profile schema as of a set of your endpoint properties the are controlled by your server-side applications. 
For example, client subscription plan, device activation flag, etc. You may also use server-side endpoint profile to store properties that are set 
during manufacturing and should not be controlled by client application.

Server-side endpoint profile is designed to be accessed and modified by various server-side applications through Kaa REST API integration layer. Server-side profile is 
not accessible from endpoint SDK or other client application logic but you can configure your own server-side profile schema using the 
[Admin UI](#setting-server-side-endpoint-profile-schema-from-admin-ui) or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO). 
For the purpose of this guide we will use a fairly abstract server-side profile schema shown below.

```json
{  
   "type":"record",
   "name":"ServerProfile",
   "namespace":"org.kaaproject.kaa.schema.sample.profile",
   "fields":[  
      {  
         "name":"subscriptionPlan",
         "type":"string"
      },
      {  
         "name":"activationFlag",
         "type":"boolean"
      }
   ]
}
```

Once this schema is configured, you are able to assign server-side endpoint profile body to certain endpoints based on their ids using 
[Admin UI](#setting-server-side-endpoint-profile-schema-from-admin-ui) or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO).

### Setting server-side endpoint profile schema from Admin UI

The list of server-side endpoint profile schemas created by a tenant developer for the application is shown in the Server-side EP profile schemas window, which can be 
opened from the application menu on the navigation panel as illustrated by the following screenshot.

<img src="admin-ui/Server-side endpoint profile schema.png">

To export the server-side EP profile schema, click Export in the last column of desired schema row and select export method from drop-down. See
[CT schema export support]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library/#ct-schema-export-support) for available export methods details.
As a tenant developer, you can create new server-side EP schemas for the application as follows:

1. In the **Server-side EP profile schemas** window for the application, click **Add schema**.
2. In the **Add profile schema** window enter the name of the schema.
3. Then create a schema using one of the two options:
    1. Using the existing CT by clicking **Select existing type** and selecting exiting CT version from FQN and version drop-downs.
    
    <img src="admin-ui/Create server-side endpoint profile schema 1.png">
    
    2. Create new CT by clicking **Create new type**. In this case you will be redirected to **Add new type** window. Here you can create a schema either by using the 
    schema form or by uploading a schema in the [Avro](http://avro.apache.org/docs/current/spec.html) format from a file.
    
    <img src="admin-ui/Create server-side endpoint profile schema 2.png">
    
4. Click **Add** at the top of the window to save the schema.

If you want to review the added Avro schema, open the corresponding **Server-side EP profile schema** window by clicking the schema in the **Server-side EP profile schemas** window.

<img src="admin-ui/View server-side endpoint profile schema.png">

### REST API for Server-side endpoint profile

Use this link [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO) for getting more information.

## System part of endpoint profile ##

The system part of the endpoint profile is used by Kaa internally for its functions and contains the following information.

| Property                |Description                                                                        | 
|-------------------------------------------------------------------------------------------------------------|
|Server-side Profile Hash | The server-side profile hash used to validate integrity of endpoint profile state |
|Server Key Hash          | The hash of the server that was used during the last endpoint request             |
|SDK token                | The endpoint SDK identifier                                                       |
|Endpoint Public Key      | The public key used for security purposes to validate endpoint requests           |
|Endpoint group state     | The list of endpoint groups that contain current endpoint.                        |
|Client-side Profile Hash | The client-side profile hash used to validate integrity of endpoint profile state |

## Further reading

* [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups)
* [Configuration]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/)
* [Notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/)
* [Data-collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection)
* [Common Type Library (CTL)]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library)