---
layout: page
title: Endpoint ownership
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

To exchange events between several endpoints, it is required that those endpoints were attached to the same owner.
Depending on the application, owners may be person, groups of people, or organizations. 
As an example we can take your mobile phone or smart watch. 
These devices are your possessions (attached to you) and you are owner of these devices. 
Another example is smart TV located in your house and all your family members are users of this device. 
In this case your family is group of people (owner) that own this smart TV and smart TV is endpoint attached to this group.
Kaa provides necessary APIs to attach/detach endpoints to/from owners through one of the following two flows:

* [Owner access token flow](#owner-access-token-flow)
* [Endpoint access token flow](#endpoint-access-token-flow)


# Owner access token flow #
In the *owner access token flow*, the owner authenticates himself in an external authentication system and obtains the *access token*. 
The owner performs this authentication from the endpoint which is due to be registered with him in the Kaa instance. 
Then, Kaa SDK transfers this token to the Kaa cluster over a secure channel. The Kaa cluster verifies the access token and attaches the endpoint to the owner.


![Owner access token flow](owner_attach_2.png "Owner access token flow")


## Owner verifiers ##
The owner verification is handled by specific server components called owner verifiers. 
There are several default owner verifier implementations that are available out of the box for each Kaa installation. 
This section contains general information about the architecture, configuration and administration of the default owner verifiers. 
It is also possible to plug in [custom verifier implementations](#custom-owner-verifier). Each Kaa application can support multiple owner verifiers.
You can add new verifier from Administration UI or with using [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier).
The following image example illustrates how to add new verifier from Administration UI.

![new verifier](Admin-ui/adding-new-verifier.png "new verifier")
 
## Trustful owner verifier ##
This owner verifier implementation is created for the test and debug purposes and always accepts provided owner id and access token. 
It is recommended that you do not use this verifier in production because it may cause security issues. 
There is no specific configuration for this verifier, because its schema is empty.
To create a trustful owner verifier, use either Administration UI or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier). 
The following image example illustrates how to create a trustful owner verifier from Administration UI.

![trustful verifier](Admin-ui/verifier-trustful.png "trustful verifier")

## Facebook owner verifier ## 
This owner verifier implementation is created for verification of Facebook accounts. It is especially useful for applications that are already integrated with Facebook.


### Configuration ###
The configuration should match the following Avro schema. Note that you need to create a [facebook application](https://developers.facebook.com/products/login/) 
and specify its application id and [secret](https://developers.facebook.com/docs/graph-api/securing-requests) in the configuration.

```json
{
        "namespace": "org.kaaproject.kaa.server.verifiers.facebook.config.gen",
        "type": "record",
        "name": "FacebookAvroConfig",
        "fields": [
        {
            "name": "app_id",
            "displayName": "Application id",
            "type": "string"
        },
        {
            "name": "app_secret",
            "displayName": "Application secret",
            "type": "string"
        },
        {
           "name": "max_parallel_connections",
           "displayName": "Maximal number of allowed connections per verifier",
           "type": "int", "by_default": "5"
        }
    ]
}
```

The following configuration example matches the previous schema.

```json
{
    "app_id":"XXXXXXXXXXXXXXXX",
    "app_secret":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    "max_parallel_connections":"10"
}
```


### Administration ###
To create a Facebook owner verifier, use either Administration UI or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier).
The following image example illustrates how to create a Facebook owner verifier from Administration UI.

![facebook verifier](Admin-ui/verifier-facebook.png "facebook verifier")

## Google+ owner verifier ##
This owner verifier implementation is created for verification of Google+ accounts. It is especially useful for applications that are already integrated with Google+.

### Configuration ###
The configuration should match the following Avro schema.

```json
{
 "namespace": "org.kaaproject.kaa.server.verifiers.gplus.config.gen",
 "type": "record",
 "name": "GplusAvroConfig",
 "fields": [
    {
    "name":"max_parallel_connections",
    "displayName": "Maximum parallel connections opened",
    "type":"int", "by_default" : "20"
    },
    {
    "name":"min_parallel_connections",
    "displayName": "Minimum parallel connections opened",
    "type":"int", "by_default" : "2"
    },
    {
    "name":"keep_alive_time_milliseconds",
    "displayName": "Milliseconds to keep connection alive",
    "type":"long", "by_default" : "60000"
    }
 ]
}
```

### Administration ###
To create a Google+ owner verifier, use either Administration UI or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier).
The following image example illustrates how to create a Google+ owner verifier from Administration UI.

![google verifier](Admin-ui/verifier-google.png "google verifier")

## Twitter owner verifier ##
This owner verifier implementation is created for verification of Twitter accounts. It is especially useful for applications that are already integrated with Twitter.

### Configuration ###
The configuration should match the following Avro schema.

```json 
{
    "namespace": "org.kaaproject.kaa.server.verifiers.twitter.config.gen",
    "type": "record",
    "name": "TwitterAvroConfig",
    "fields": [
        {
            "name": "consumer_key",
            "displayName": "Consumer Key",
            "type": "string"
        },
        {
            "name": "consumer_secret",
            "displayName": "Consumer Secret",
            "type": "string"
        },
        {
            "name": "max_parallel_connections",
            "displayName": "Maximal number of allowed connections per verifier",
            "type": "int", "by_default": "5"
        }
    ]
}
```

### Administration ###
To create a Twitter owner verifier, use either Administration UI or [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier).
The following image example illustrates how to create a Twitter owner verifier from Administration UI.
                                   
![twitter verifier](Admin-ui/verifier-twitter.png "twitter verifier")

## Custom owner verifier ##
It is possible to implement and plug-in custom owner verifiers. You can find corresponding instructions on the 
[Creating custom owner verifier]({{root_url}}Customization-guide/Kaa-Server/Development-environment-setup/#creating-custom-user-verifier) page.


# Endpoint access token flow #
In the *endpoint access token flow*, new endpoints are attached to the owner with the help of the endpoint which was attached to the owner beforehand.
The following steps illustrate this flow with the endpoint A, which is already attached to the owner, and the endpoint B, which is due to be attached:

   1. The endpoint B periodically generates and sends its access token to the Kaa cluster.
   2. The endpoint B displays its access token as a QR code on the screen (TV) or on the webpage (e.g., a router or other device with an embedded server).
   3. The endpoint A retrieves this token by scanning QR code or in any other suitable way and sends it in the endpoint attach request to the Kaa cluster.
   4. The Kaa cluster verifies the access code and attaches the endpoint B to the owner of the endpoint A.


![Owner access token flow](endpoint_attach_2.png "Owner access token flow")


## Attach endpoint to owner ##

To enable sending/receiving events to/from endpoints, at first the client should attach the endpoint to the owner as shown in the following examples for different platforms SDK.

> Please refer to [Glossary]({{root_url}}Glossary/) for more details about **userExternalId**, **userAccessToken**, **userVerifierToken** 
>and other parameters which are used by KaaClient methods.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus">C++</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.event.registration.UserAuthResultListener;
 
/**
* Creates owner attach request using default verifier. Default verifier is selected during SDK generation.
* If there was no default verifier selected this method will throw runtime exception.
*/

kaaClient.attachUser("userExternalId", "userAccessToken", new UserAttachCallback()
{
    @Override
    public void onAttachResult(UserAttachResponse response) {
        System.out.println("Attach response" + response.getResult());
    }
});



/**
* Creates owner attach request using specified verifier.
*/
kaaClient.attachUser("userVerifierToken", "userExternalId", "userAccessToken", new UserAttachCallback()
{
    @Override
    public void onAttachResult(UserAttachResponse response) {
        System.out.println("Attach response" + response.getResult());
    }
});
```

</div>
<div id="C_plus_plus" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/event/registration/SimpleUserAttachCallback.hpp>
 
using namespace kaa;
 
class SimpleUserAttachCallback : public IUserAttachCallback {
public:
    virtual void onAttachSuccess()
    {
        std::cout << "Endpoint is attached to a user" << std::endl;
    }
 
    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Failed to attach endpoint to a user: error code " << errorCode << ", reason '" << reason << "'" << std::endl;
    }
};
 
...
  
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
 
// Start an endpoint
kaaClient->start();
 
// Try to attach an endpoint to a user
kaaClient->attachUser("userExternalId", "userAccessToken", std::make_shared<SimpleUserAttachCallback>());
```

</div>
<div id="C" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_user.h>
#include <kaa/platform/ext_user_callback.h>
 
kaa_client_t *kaa_client = /* ... */;
 
kaa_error_t on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_detached(void *context, const char *endpoint_access_token)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_attach_success(void *context)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    return KAA_ERR_NONE;
}
kaa_attachment_status_listeners_t attachement_listeners = 
{
        NULL,
        &on_attached,
        &on_detached,
        &on_attach_success,
        &on_attach_failed
};
/* Assume Kaa SDK is already initialized */
kaa_error_t error_code = kaa_user_manager_set_attachment_listeners(kaa_client_get_context(kaa_client)->user_manager
                                                                 , &attachement_listeners);
/* Check error code */
error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                   , "userExternalId"
                                                   , "userAccessToken");
/* Check error code */
```

</div>
<div id="Objective-C" class="tab-pane fade" markdown="1" >

```objc
#import <Kaa/Kaa.h>
 
@interface ViewController () <UserAttachDelegate>
 
...
 
- (void)prepareUserForMessaging {
    [self.kaaClient attachUserWithId:@"userExternalId" accessToken:@"userAccessToken" delegate:self];
}
- (void)onAttachResult:(UserAttachResponse *)response {
    NSLog(@"Attach response: %i", response.result);
}
```

</div>
</div>


## Assisted attach ##

Specific endpoint may not be able to attach itself independently. E.g. in case if endpoint doesn't have an owner access token. 
Another endpoint that already attached can assist in attachment process of the new endpoint. Below are examples of assisted attachment.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-1">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-1">C++</a></li>
  <li><a data-toggle="tab" href="#C-1">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-1">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-1" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
 
/**
* Updates with new endpoint attach request
*
* @param   endpointAccessToken Access token of the attaching endpoint
* @param   resultListener      Listener to notify about result of the endpoint attaching
*
*/

kaaClient.attachEndpoint("endpointAccessToken", new OnAttachEndpointOperationCallback()
{
    @Override
    public void onAttach(SyncResponseResultType result, EndpointKeyHash resultContext) {
        //
    }
});

```

</div>
<div id="C_plus_plus-1" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/event/registration/SimpleUserAttachCallback.hpp>
 
using namespace kaa;
 
class SimpleEndpointAttachCallback : public IAttachEndpointCallbackPtr {
public:
    virtual void onAttachSuccess()
    {
        std::cout << "Endpoint is attached to a user" << std::endl;
    }
 
    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Failed to attach endpoint to a user: error code " << errorCode << ", reason '" << reason << "'" << std::endl;
    }
};
 
...
  
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
 
// Start an endpoint
kaaClient->start();
 
// Try to attach an endpoint to a user
kaaClient->attachEndpoint("endpointAccessToken", std::make_shared<SimpleEndpointAttachCallback>());
```

</div>
<div id="C-1" class="tab-pane fade" markdown="1" >


```c
#include <stdint.h>
#include <kaa/kaa_error.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_user.h>
#include <kaa/platform/ext_user_callback.h>
 
kaa_client_t *kaa_client = /* ... */;
 
kaa_attachment_status_listeners_t assisted_attachment_listeners =  /* ... */;
 
/* Assume Kaa SDK is already initialized */
 
kaa_error_t error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                                , "userExternalId"
                                                                , "userAccessToken");
 
/* Check error code */
 
error_code = kaa_user_manager_attach_endpoint(kaa_client_get_context(kaa_client)->user_manager, "externalAccessToken", &assisted_attachment_listeners);
 
/* Check error code */
```

</div>
<div id="Objective-C-1" class="tab-pane fade" markdown="1" >

```objc
#import <Kaa/Kaa.h>
 
@interface ViewController () <OnAttachEndpointOperationDelegate>
 
...
- (void)prepareEndpointForMessaging {
    [self.kaaClient attachEndpointWithAccessToken:@"userExternalId" delegate:self];
}

- (void)onAttachResult:(SyncResponseResultType)result withEndpointKeyHash:(EndpointKeyHash *)endpointKeyHash{
    NSLog(@"Attach response: %i", result);
}
```

</div>
</div>

## Detach endpoint from owner ##

Another endpoint that already attached can assist in detachment process for another endpoint which attached too. Below are examples of assisted detachment.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-2">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-2">C++</a></li>
  <li><a data-toggle="tab" href="#C-2">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-2">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-2" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;

/**
* Updates with new endpoint detach request
*
* @param   endpointKeyHash Key hash of the detaching endpoint
* @param   resultListener Listener to notify about result of the enpoint attaching
*
*/

kaaClient.detachEndpoint("endpointKeyHash", new OnDetachEndpointOperationCallback()
{
    @Override
    public void onDetach(SyncResponseResultType result) {
        //
    }
});
```

</div>
<div id="C_plus_plus-2" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/event/registration/SimpleUserAttachCallback.hpp>
 
using namespace kaa;
 
class SimpleEndpointDetachCallback : public IDetachEndpointCallbackPtr {
public:
    virtual void onDetachSuccess()
    {
        std::cout << "Endpoint is attached to a user" << std::endl;
    }
 
    virtual void onDetachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Failed to detach endpoint from user: error code " << errorCode << ", reason '" << reason << "'" << std::endl;
    }
};
 
...
  
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
 
// Start an endpoint
kaaClient->start();
 
// Try to attach an endpoint to a user
kaaClient->detachEndpoint("endpointKeyHash", std::make_shared<SimpleEndpointDetachCallback>());
```

</div>
<div id="C-2" class="tab-pane fade" markdown="1" >

```c
#include <stdint.h>
#include <kaa/kaa_error.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_user.h>
#include <kaa/platform/ext_user_callback.h>
 
kaa_client_t *kaa_client = /* ... */;
 
kaa_attachment_status_listeners_t assisted_detachment_listeners =  /* ... */;
 
/* Assume Kaa SDK is already initialized */
 
kaa_error_t error_code = kaa_user_manager_detach_endpoint(kaa_client_get_context(kaa_client)->user_manager
                                                                , "endpointHashKey"
                                                                , &assisted_detachment_listeners);
 
/* Check error code */
 
error_code = kaa_user_manager_detach_endpoint(kaa_client_get_context(kaa_client)->user_manager, "endpointHashKey", &assisted_detachment_listeners);
 
/* Check error code */
```

</div>
<div id="Objective-C-2" class="tab-pane fade" markdown="1" >

```objc
#import <Kaa/Kaa.h>
 
@interface ViewController () <DetachEndpointFromUserDelegate>
 
...
- (void)prepareEndpointForMessaging {
    [self.kaaClient detachEndpointWithKeyHash:@"endpointAccessToken" delegate:self];
}

- (void)onDetachResult:(SyncResponseResultType)result{
    NSLog(@"Detache response : %i", result);
}
```

</div>
</div>