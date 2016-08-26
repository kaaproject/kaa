---
layout: page
title: Endpoint profiles
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

[Endpoint profile]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) (EP profile) is a structured set of data that describes specific characteristics of an [endpoint]({{root_url}}Glossary/#endpoint-ep).

The structure of both client-side and server-side endpoint profile is a customizable structured data set that describes specific characteristics of the endpoint.
The profiles are used to attribute endpoints to [endpoint groups]({{root_url}}Glossary/#endpoint-group).
Every endpoint profile comprises the *client-side*, *server-side* and *system* parts.

The structure of both client-side and server-side parts of an endpoint profile are defined by [application]({{root_url}}Glossary/#kaa-application) developer using the [Apache Avro schema](http://avro.apache.org/docs/current/spec.html#schemas) format.
Application developer can reuse and share certain data structures using the [common type library (CTL)]({{root_url}}Glossary/#common-type-library-ctl).

The client-side structure is used during the SDK generation.
Therefore, if you make changes to the client-side structure, you need to re-generate your SDK.
For more information, see [Endpoint SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs).

The structure of the system part is identical across the applications and is used by [Kaa platform]({{root_url}}Glossary/#kaa-platform) to perform internal functions.
Both client-side and server-side profile schemas are maintained within the corresponding application, with its own version that distinguishes it from the previous schemas.
Multiple schema versions and the corresponding endpoint profiles can coexist within a single application.

## Client-side EP profile

Initial values for the client-side part are specified by the the client developer using data schemas for the endpoint SDK.
Then, the client-side endpoint profile is generated during registration of a new endpoint.

The client-side endpoint profile can be updated at run time using an SDK API call.
After the SDK requested a profile update, the group membership of the endpoint is re-evaluated and updated to match the new endpoint profile.
Think about the client-side profile schema as of a structured data set of your endpoint application that will later be available to you in Kaa server and may change due to your client application logic or device state.
The client-side endpoint profile is unidirectionally synchronized, and thus should not be considered as a means to temporarily store endpoint data in the [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).
When your client application logic or device state changes, the new client profile schema will then be available on [Kaa sever]({{root_url}}Glossary/#kaa-server).
Endpoints cannot retrieve the profile information back from the Kaa cluster.
The endpoint SDK does not persist the profile information over the endpoint reboots.
However, it detects profile data changes and submits the new data to the Kaa cluster as a profile update.

<img src="endpoint-profile-generation/ClientSideEndpointProfileGeneration_0_8_0.png">

### Example of a client-side EP profile

For the purpose of this guide, we will use a fairly abstract client-side profile schema.

The schema defines 4 fields:

* id --- unique identifier
* os --- operating system name
* os_version --- operating system version
* build --- build version

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

The following client-side profile is based on the rules set in the schema.

```json
{
  "id" : "1",
  "os" : "Android",
  "os_version" : "1",
  "build" : "1"
}
```

The schema structure from our example allows filtering the endpoints by the operation system of device (for example, to show only Android devices), os_version (for example, to push some notifications only for the specified os version).
You can create complex filtering conditions by combining as many filtering conditions as needed.

### Setting client-side EP profile schema from Administration UI

To view the list of client-side endpoint profile schemas created by a [tenant developer]({{root_url}}Glossary/#tenant-developer), open the the **Client-side EP profile** page under the **Schemas** section of the application.

<img src="admin-ui/Client-side endpoint profile schema.png">

To export a client-side EP profile schema, click the corresponding **Export** button and choose the export method from the drop-down list.
For more information about the schema export methods, see [CT schema export support]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library/#ct-schema-export-support).

As a tenant developer, you can create new client-side EP schemas for your application as follows:

1. Under the **Schemas** section of the application, click **Client-side EP profile schemas**, then click the **Add schema** button.

2. On the **Add profile schema** page, enter your schema name and description (optional).	

3. Then create a schema using one of the two options:

    * Use the existing [common type (CT)]({{root_url}}Glossary/#common-type-ct) by clicking **Select existing type**.
    Click the **Select fully qualified name of existing type** field and select the CT from the drop-down list, then select the version from the corresponding drop-down list.
    
    <img src="admin-ui/Create client-side endpoint profile schema 1.png">
    
    * Create new CT by clicking **Create new type**.
    The **Add new type** page will open.
    Here you can create a schema either by using the schema form or by uploading a file containing the schema in the [Avro](http://avro.apache.org/docs/current/spec.html) format.
    
    <img src="admin-ui/Create client-side endpoint profile schema 2.png">

4. Click **Add** to save your schema.

### REST API for client-side EP profile

Admin REST API provides the following actions:

* [Get profile based on endpoint key]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfileByKeyHash)
* [Get client- and server-side endpoint profile bodies based on endpoint key]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfileBodyByKeyHash)
* [Get endpoint profiles by owner ID]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfilesByUserExternalId)
* [Get client-side endpoint profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getProfileSchema)
* [Get client-side endpoint profile schemas]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getProfileSchemasByApplicationToken)
* [Delete endpoint]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/removeEndpointProfileByKeyHash)
* [Create client-side endpoint profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/saveProfileSchema)

For detailed description of the REST API, its purpose, interface, and features, see [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#/Profiling) its purpose, interfaces and features supported.

### SDK API for client-side EP profile

Endpoint profile information changes as a result of the client operation or user's actions.
The client updates the profile via the endpoint SDK API calls.
The endpoint SDK checks for profile changes by comparing the new profile hash against the previously persisted one.
When SDK detects a profile change, the endpoint profile management module sends this information to the Operations service.
The Operations sevice then updates the endpoint profile information in the database and revises the endpoint group membership.

You can configure your client-side profile schema using the [Administration UI](#setting-client-side-ep-profile-schema-from-administration-ui) or server REST API.
First, you need to [create a new CT]({{root_url}}Programming-guide/Server-REST-APIs/#!/Common_Type_Library/saveCTLSchemaWithAppToken).
Then, [create client-side endpoint profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/saveProfileSchema) containing a reference to this CT.
Client-side endpoint profile updates are reported to the endpoint SDK using a profile container.
The profile-related API varies depending on the target SDK platform, however the general approach is the same.

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
// Create profile for the endpoint
Profile profile;
profile.id = "deviceId";
profile.os_version = "3.17";
profile.os = OS::Linux;
profile.build = "0.0.1-SNAPSHOT";
 
// Set a profile container to pass a profile to an endpoint
kaaClient->setProfileContainer(std::make_shared<DefaultProfileContainer>(profile));
 
// Call each time when a profile is updated
kaaClient->updateProfile();
 
// Start the endpoint
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
    // Sample profile that is an auto-generated class based on user-defined schema.
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

## Server-side endpoint profile

The server-side endpoint profile is initially generated at the stage of a new endpoint registration.
By default, server-side profile record is auto-generated based on the latest server-side profile schema of a particular application.
In this case, endpoint membership in the endpoint groups is re-evaluated and updated to match the new endpoint profile.
To create a server-side endpoint profile using a REST API, you need to [create new CT]({{root_url}}Programming-guide/Server-REST-APIs/#!/Common_Type_Library/saveCTLSchemaWithAppToken).
Then, [create server-side profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/saveServerProfileSchema) containing a reference to this CT.

<img src="endpoint-profile-generation/ServerSideEndpointProfileGeneration_0_8_0.png">

### Example of a server-side EP profile

Below is a simple example of a server-side EP profile schema.

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
The following server-side profile is compatible with our schema example.

```json
{
    "subscriptionPlan": "plan",
    "activationFlag": "false"
}
```

The schema structure from our example allows filtering the endpoints by serial number (SN) and customer ID.
For example, you can remotely turn on/off features for certain customers or deactivate a device with certain SN.
You can create complex filtering conditions by combining as many filtering conditions as needed.

### Updating server-side EP profile

Server-side profile schema is a set of your endpoint properties the are controlled by your server-side applications.
For example, client subscription plan, device activation flag, etc.
You can also use server-side endpoint profile to store properties that are set during device manufacturing and should not be controlled by client application.

Server-side endpoint profile is designed to be accessed and modified by various server-side applications through Kaa REST API integration layer.
Server-side profile is not accessible from endpoint SDK or other client application logic, but you can configure your own server-side profile schema using the [Administration UI](#setting-server-side-ep-profile-schema-from-administration-ui) or [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/updateServerProfile).

For the purpose of this guide, we will use a fairly abstract server-side profile schema example.

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

### Setting server-side EP profile schema from Administration UI

To view the list of server-side endpoint profile schemas created by a [tenant developer]({{root_url}}Glossary/#tenant-developer), open the the **Server-side EP profile** page under the **Schemas** section of the application.

<img src="admin-ui/Server-side endpoint profile schema.png">

To export a client-side EP profile schema, click the corresponding **Export** button and choose the export method from the drop-down list.
See [CT schema export support]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library/#ct-schema-export-support) for available export methods details.

As a tenant developer, you can create new server-side EP schemas for your application as follows:

1. Under the **Schemas** section of the application, click **Server-side EP profile schemas**, then click the **Add schema** button.

2. On the **Add profile schema** page, enter your schema name and description (optional).

3. Then create a schema using one of the two options:

    * Use the existing [common type (CT)]({{root_url}}Glossary/#common-type-ct) by clicking **Select existing type**.
    Click the **Select fully qualified name of existing type** field and select the CT from the drop-down list, then select the version from the corresponding drop-down list.
    
    <img src="admin-ui/Create server-side endpoint profile schema 1.png">
    
    * Create new CT by clicking **Create new type**.
    The **Add new type** page will open.
    Here you can create a schema either by using the schema form or by uploading a file containing the schema in the [Avro](http://avro.apache.org/docs/current/spec.html) format.
    
    <img src="admin-ui/Create server-side endpoint profile schema 2.png">
    
4. Click **Add** to save your schema.

### REST API for server-side EP profile

Use the server REST API to perform the following actions:

* [Get profile based on endpoint key]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfileByKeyHash)
* [Get client- and server-side endpoint profile bodies based on endpoint key]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfileBodyByKeyHash)
* [Get endpoint profiles by owner ID]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getEndpointProfilesByUserExternalId)
* [Delete endpoint]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/removeEndpointProfileByKeyHash)
* [Create server-side endpoint profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/saveServerProfileSchema)
* [Get server-side endpoint profile schema]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getServerProfileSchema)
* [Get server-side endpoint profile schemas]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/getServerProfileSchemasByApplicationToken)
* [Update server-side endpoint profile]({{root_url}}Programming-guide/Server-REST-APIs/#!/Profiling/updateServerProfile)

For detailed description of the REST API, its purpose, interface, and features, see [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#/Profiling) its purpose, interfaces and features supported.

## System part of EP profile

The system part of the endpoint profile is used by Kaa platform to perform internal functions.
Below is the list of system part properties.

| Property                |Description                                                                        |
|-------------------------------------------------------------------------------------------------------------|
|Server-side profile hash | Server-side profile hash used to validate integrity of endpoint profile state.    |
|Server key hash          | Server hash used during the last endpoint request.                                |
|SDK token                | Endpoint SDK identifier.                                                          |
|Endpoint public key      | Public key used for security purposes to validate endpoint requests.              |
|Endpoint group state     | List of endpoint groups that contain current endpoint.                            |
|Client-side profile hash | Client-side profile hash used to validate integrity of endpoint profile state.    |

## Further reading

* [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups)
* [Configuration]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/)
* [Notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/)
* [Data-collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection)
* [Common Type Library (CTL)]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library)