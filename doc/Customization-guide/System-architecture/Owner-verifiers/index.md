---
layout: page
title: Owner verifiers
permalink: /:path/
nav: /:path/Customization-guide/System-architecture/Owner-verifiers
sort_idx: 10
---

* [Configuration schema](#configuration-schema)
* [User verifier implementation](#user-verifier-implementation)
* [User verifier descriptor](#user-verifier-descriptor)
* [User verifier provisioning](#user-verifier-provisioning)

To implement a custom user verifier, you need to complete the following steps.

1. Design and compile a configuration schema.
2. Implement the user verifier based on AbstractKaaUserVerifier.
3. Develop the user verifier descriptor.
4. Provision the user verifier.

We recommend that you use one of the [existing user verifier ]()[implementations]() as a reference.

## Configuration schema

A user verifier configuration schema is an Avro compatible schema that defines configuration parameters of the user verifier. The following parameters in the schema affect Kaa Admin UI layout.

* displayName - displays the name of the field on UI
* by\_default - displays the default value of the field on UI  
  
```json
    {
     "namespace": "org.kaaproject.kaa.schema.sample",
     "type": "record",
     "name": "CustomUserVerifierConfiguration",
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
Once you have prepared your schema, you can compile it using the following command.  

```
java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>
```

Please refer to [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema) for more information. It is also possible to integrate the schema compilation with [avro-maven-plugin](http://avro.apache.org/docs/current/gettingstartedjava.html).

## User verifier implementation

All Kaa user verifiers extend generic abstract class org.kaaproject.kaa.server.common.verifier.AbstractUserVerifier<T>. The following code example illustrates the implementation of a custom user verifier.

```java
    package org.kaaproject.kaa.sample.verifier;
    
    import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
    import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
    import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
    import org.kaaproject.kaa.schema.sample.CustomUserVerifierConfiguration;
    
    /**
     * 
     * Sample user verifier implementation that uses {@link CustomUserVerifierConfiguration} as configuration.
     *
     */
    public class CustomUserVerifier extends AbstractKaaUserVerifier<CustomUserVerifierConfiguration> {
        /**
        * Initialize a user verifier instance with a particular configuration and
        * common transport properties. The configuration is a serialized Avro
        * object. The serialization is done using the schema specified in
        * {@link KaaUserVerifierConfig}.
        *
        * @param context the user verifier initialization context
        * @param configuration the configuration object that you have specified during verifier provisioning.
        */
        @Override
        public void init(UserVerifierContext context, CustomUserVerifierConfiguration configuration) {
    
        }
        
        /**
        * Verifies the access token.
        *
        * @param userExternalId the user external id
        * @param userAccessToken the access token
        * @param callback User verification callback, which helps to identify verification status and
        * possible reason failure
        * @return true, if verified
        */
        @Override
        public void checkAccessToken(String userExternalId, String userAccessToken, UserVerifierCallback callback) {
    
        }
        
        /**
        * Starts a user verifier instance. This method should block its caller thread
        * until the user verifier is started. This method should not block its caller
        * thread after startup sequence is successfully completed.
        */
        @Override
        public void start() {
    
        }
        
        /**
        * Stops the user verifier instance. This method should block its current thread
        * until user verifier is stopped. User verifier may be started again after it is
        * stopped.
        */
        @Override
        public void stop() {
    
        }
        
        /**
        * Gets the configuration class.
        *
        * @return the configuration class
        */
        @Override
        public Class<CustomUserVerifierConfiguration> getConfigurationClass() {
            return CustomUserVerifierConfiguration.class;
        }
    }
```

## User verifier descriptor

A user verifier descriptor provides Kaa with the information on how to locate and configure your custom user verifier. To implement a user verifier descriptor, you need to implement the PluginConfig interface at first.

It is also important to provide your class with the @KaaPluginConfig annotation. This annotation helps Kaa Admin UI to find all available user verifiers in the class path.

>**NOTE:** A user verifier descriptor is optional if you are going to configure your user verifiers using only REST API.

The following code example illustrates the implementation of a user verifier descriptor. 

```java
    package org.kaaproject.kaa.sample.verifier.config;
    
    import org.apache.avro.Schema;
    import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
    import org.kaaproject.kaa.server.common.plugin.PluginConfig;
    import org.kaaproject.kaa.server.common.plugin.PluginType;
    import org.kaaproject.kaa.schema.sample.CustomUserVerifierConfiguration;
    
    @KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
    public class CustomUserVerifierConfig implements PluginConfig {
        
        /**
        * Returns the plugin display name. There is no strict rule for this
        * name to be unique.
        * 
        * @return the plugin display name
        */
        @Override
        public String getPluginTypeName() {
            return TRUSTFUL_VERIFIER_NAME;
        }
        
        /**
        * Returns the class name of the plugin implementation.
        *
        * @return the class name of the plugin implementation
        */
        @Override
        public String getPluginClassName() {
            return "org.kaaproject.kaa.schema.sample.verifier.CustomUserVerifier";
        }
        
        /**
        * Returns the avro schema of the plugin configuration.
        *
        * @return the avro schema of the plugin configuration
        */
        @Override
        public Schema getPluginConfigSchema() {
            return CustomUserVerifierConfiguration.SCHEMA$;
        }
    }
```

## User verifier provisioning

To provision your user verifier, do the following:

1. Create and compile user verifier configuration schema.
2. Create and compile user verifier implementation.
3. Create and compile user verifier descriptor.
4. Place the user verifier descriptor and configuration classes into the Admin UI class path. Usually it is ```/usr/lib/kaa-node/lib```.
5. Place the user verifier implementation classes into the Operations Server class path (```/usr/lib/kaa-node/lib```).
6. Use [Admin UI]() or [REST API]() to create/update/delete your user verifier instances.

---
