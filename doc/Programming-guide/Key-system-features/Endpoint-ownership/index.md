---
layout: page
title: Endpoint ownership
permalink: /:path/
nav: /:path/Programming-guide/Key-system-features/Endpoint-ownership
sort_idx: 50
---


Endpoint ownership

* [User access token flow](#user-access-token-flow)
  * [User verifiers](#user-verifiers)
    * [Trustful user verifier](#trustful-user-verifier)
    * [Facebook user verifier](#facebook-user-verifier)
      * [Configuration](#configuration)
      * [Administration](#administration)
    * [Google+ user verifier](#google-user-verifier)
      * [Configuration](#configuration-1)
      * [Administration](#administration-1)
    * [Twitter user verifier](#twitter-user-verifier)
      * [Configuration](#configuration-2)
      * [Administration](#administration-2)
    * [Custom user verifier](#custom-user-verifier)
* [Endpoint access token flow](#endpoint-access-token-flow)


To use events between several endpoints, it is required that those endpoints were attached to the same user (in other words, registered with the same user). Kaa provides necessary APIs to attach/detach endpoints to/from users through one of the following two flows:
* [User access token flow] ()
* [Endpoint access token flow] ()


### User access token flow ###
In the *user access token flow*, the user authenticates himself in an external authentication system and obtains the *access token*. The user performs this authentication from the endpoint which is due to be registered with him in the Kaa instance. Then, Kaa SDK transfers this token to the Kaa cluster over a secure channel. The Kaa cluster verifies the access token and attaches the endpoint to the user.


![User access token flow](user_attach_2.png "User access token flow")


### User verifiers ###
The user verification is handled by specific server components called user verifiers. There are several default user verifier implementations that are available out of the box for each Kaa installation. This section contains general information about the architecture, configuration and administration of the default user verifiers. It is also possible to plug in [custom verifier implementations](). Each Kaa application can support multiple user verifiers.
 
#### Trustful user verifier ####
This user verifier implementation is created for the test and debug purposes and always accepts provided user id and access token. It is recommended that you do not use this verifier in production because it may cause security issues. There is no specific configuration for this verifier, because its schema is empty.
To create a trustful user verifier, use either [Admin UI]() or [REST API](). The following REST API call example illustrates how to create a new trustful user verifier.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier", "pluginTypeName":"Trustful", "applicationId": "150", "name":"MyTrustfulUserVerifier","description": "Sample description", "jsonConfiguration": "{}"}' "http://10.2.1.191:8080/kaaAdmin/rest/api/userVerifier" | python -mjson.tool
```

### Facebook user verifier ### 
This user verifier implementation is created for verification of Facebook accounts. It is especially useful for applications that are already integrated with Facebook.


### Configuration ###
The configuration should match the following Avro schema. Note that you need to create a [facebook application](https://developers.facebook.com/products/login/) and specify its application id and secret in the configuration.

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
To create a Facebook user verifier, use either Admin UI or REST API. The following REST API call example illustrates how to create a new Facebook user verifier.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.verifiers.facebook.verifier.FacebookUserVerifier", "pluginTypeName":"Facebook", "applicationId": "150", "name":"MyFacebookVerifier","description": "Sample description", "jsonConfiguration": "{\"app_id\":\"5215235\",\"app_secret\":\"123424\", \"max_parallel_connections\":10}"}' "http://localhost:8080/kaaAdmin/rest/api/userVerifier" | python -mjson.tool
```

### Google+ user verifier ### 
This user verifier implementation is created for verification of Google+ accounts. It is especially useful for applications that are already integrated with Google+.

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
To create a Google+ user verifier, use either [Admin UI]() or [REST API](). The following REST API call example illustrates how to create a new Google+ user verifier.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.verifiers.gplus.verifier.GplusUserVerifier", "pluginTypeName":"Google+ verifier", "applicationId": "150", "name":"GplusVerifier","description": "Sample description", "jsonConfiguration": "{\"max_parallel_connections\":20, \"min_parallel_connections\":2, \"keep_alive_time_milliseconds\":60000}"}' "http://localhost:8080/kaaAdmin/rest/api/userVerifier" | python -mjson.tool
```

### Twitter user verifier ###
This user verifier implementation is created for verification of Twitter accounts. It is especially useful for applications that are already integrated with Twitter.

### Configuration ##
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
To create a Twitter user verifier, use either [Admin UI]() or [REST API](). The following REST API call example illustrates how to create a new Twitter user verifier.

```bash
curl -v -S -u devuser:devuser123 -X POST -H 'Content-Type: application/json' -d'{"pluginClassName": "org.kaaproject.kaa.server.verifiers.twitter.verifier.TwitterUserVerifier", "pluginTypeName":"Twitter", "applicationId": "110", "name":"MyTwitter","description": "Sample description", "jsonConfiguration": "{\"consumer_key\":\"XXXXXXXXXXXXXXXXXXX\", \"consumer_secret\":\"XXXXXXXXXXXXXXXXX\", \"max_parallel_connections\": 5, \"twitter_verify_url\": \"https:\/\/api.twitter.com\/1.1\/account\/verify_credentials.json\"}"}' "http://localhost:8080/kaaAdmin/rest/api/userVerifier" | python -mjson.tool
```

### Custom user verifier ###
It is possible to implement and plug-in custom user verifiers. You can find corresponding instructions on the [Creating custom user verifier]() page.


## Endpoint access token flow ##
In the *endpoint access token flow*, new endpoints are attached to the user with the help of the endpoint which was attached to the user beforehand.
The following steps illustrate this flow with the endpoint A, which is already attached to the user, and the endpoint B, which is due to be attached.
	1.The endpoint B periodically generates and sends its access token to the Kaa cluster.
	2.The endpoint B displays its access token as a QR code on the screen (TV) or on the webpage (e.g., a router or other device with an embedded server).
	3.The endpoint A retrieves this token by scanning QR code or in any other suitable way and sends it in the endpoint attach request to the Kaa cluster.
	4.The Kaa cluster verifies the access code and attaches the endpoint B to the user of the endpoint A.


![User access token flow](endpoint_attach_2.png "User access token flow")
