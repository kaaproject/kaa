---
layout: page
title: Owner verifiers
permalink: /:path/
sort_idx: 40
---

* TOC
{:toc}

{% include variables.md %}

This guide contains information about how to create custom owner verifiers and add them to existed. Please refer to [Endpoint ownership]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership) for information about default owner verifiers
and access token flow.


To implement a custom owner verifier, you need to complete the following steps.

1. Design and compile a configuration schema.
2. Implement the owner verifier based on AbstractKaaUserVerifier.
3. Develop the owner verifier descriptor.
4. Provision the owner verifier.

We recommend that you use one of the [existing owner verifier implementations](https://github.com/kaaproject/kaa/tree/master/server/verifiers) as a reference.

## Configuration schema

A owner verifier configuration schema is an Avro compatible schema that defines configuration parameters of the owner verifier. The following parameters in the schema affect Kaa Admin UI layout.

* displayName - displays the name of the field on UI
* by_default - displays the default value of the field on UI  
  
```json
    {
     "namespace": "org.kaaproject.kaa.sample.verifier.config.gen", 
     "type": "record",
     "name": "CustomOwnerVerifierConfiguration",
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

## Owner verifier implementation

All Kaa owner verifiers extend generic abstract class org.kaaproject.kaa.server.common.verifier.AbstractUserVerifier<T>. The following code example illustrates the implementation of a custom owner verifier.

```java
    package org.kaaproject.kaa.sample.verifier;
    
    import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
    import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
    import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
    import org.kaaproject.kaa.sample.verifier.config.gen.CustomOwnerVerifierConfiguration;
    
    /**
     * 
     * Sample owner verifier implementation that uses {@link CustomOwnerVerifierConfiguration} as configuration.
     *
     */
    public class CustomOwnerVerifier extends AbstractKaaUserVerifier<CustomOwnerVerifierConfiguration> {
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
        public void init(UserVerifierContext context, CustomOwnerVerifierConfiguration configuration) {
    
        }
        
        /**
        * Verifies the access token.
        *
        * @param userExternalId the user external id
        * @param userAccessToken the access token
        * @param callback User verification callback, which helps to identify verification status and
        * possible reason failure
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
        public Class<CustomOwnerVerifierConfiguration> getConfigurationClass() {
            return CustomOwnerVerifierConfiguration.class;
        }
    }
```

## Owner verifier descriptor

A owner verifier descriptor provides Kaa with the information on how to locate and configure your custom owner verifier. To implement a owner verifier descriptor, you need to implement the PluginConfig interface at first.

It is also important to provide your class with the @KaaPluginConfig annotation. This annotation helps Kaa Admin UI to find all available owner verifiers in the class path.

>**NOTE:** A owner verifier descriptor is optional if you are going to configure your owner verifiers using only REST API.

The following code example illustrates the implementation of a owner verifier descriptor. 

```java
    package org.kaaproject.kaa.sample.verifier.config;
    
    import org.apache.avro.Schema;
    import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
    import org.kaaproject.kaa.server.common.plugin.PluginConfig;
    import org.kaaproject.kaa.server.common.plugin.PluginType;
    import org.kaaproject.kaa.sample.verifier.config.gen.CustomOwnerVerifierConfiguration;
    
    @KaaPluginConfig(pluginType = PluginType.USER_VERIFIER)
    public class CustomOwnerVerifierConfig implements PluginConfig {
        
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
            return "org.kaaproject.kaa.schema.sample.verifier.CustomOwnerVerifier";
        }
        
        /**
        * Returns the avro schema of the plugin configuration.
        *
        * @return the avro schema of the plugin configuration
        */
        @Override
        public Schema getPluginConfigSchema() {
            return CustomOwnerVerifierConfiguration.SCHEMA$;
        }
    }
```

## Owner verifier provisioning

To provision your owner verifier, do the following:

1. Create maven project. You can use this [pom](https://github.com/kaaproject/kaa/blob/master/server/verifiers/trustful-verifier/pom.xml) as an example. 
2. Create similar classes as defined above and put them in appropriate packages.
3. Add your verification logic to methods of `CustomOwnerVerifier` and build your project using next command: 
<br/>
```$ mvn clean install```
4. Place created jar file into _/usr/lib/kaa-node/lib_.
5. If you using different package than _org.kaaproject.kaa.server.verifiers.*_  you need to edit kaa-node.properties file in /usr/lib/kaa-node/conf folder. Specify additional package to scan kaa plugins configuration in parameter additional_plugins_scan_package, 
   in our case -- _org.kaaproject.kaa.sample_.
6. Restart kaa-node service:
<br/>
``` $ sudo service kaa-node restart```
7. Use [Admin UI]({{root_url}}Administration-guide/Tenants-and-applications-management/#adding-user-verifiers) or [REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier) to create/update/delete your owner verifier instances.

---
