---
layout: page
title: Owner verifiers
permalink: /:path/
sort_idx: 40
---

* TOC
{:toc}

{% include variables.md %}

This guide explains how you can create and implement custom owner verifiers in addition to the existing ones.
See [Endpoint ownership]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership) for information about default owner verifiers and access token flow.

The process comprises the following steps.

1. Design and compile a configuration schema.

2. Implement the owner verifier based on the [AbstractKaaUserVerifier]({{github_url}}server/common/verifier-shared/src/main/java/org/kaaproject/kaa/server/common/verifier/AbstractKaaUserVerifier.java) class.

3. Develop the owner verifier descriptor.

4. Provision the owner verifier.

It is recommended that you use one of the [existing owner verifier implementations]({{github_url}}server/verifiers) as a reference.

## Configuration schema

Owner verifier configuration schema is an Avro compatible schema that defines configuration parameters of the owner verifier.
Use the following schema parameters to configure Kaa [Administration UI]({{root_url}}Glossary/#administration-ui):

* `displayName` -- displays the UI field name.
* `by_default` -- displays the UI field default value.

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

Once you prepared your schema, compile it using the following command.

```
java -jar /path/to/avro-tools-1.7.7.jar compile schema <schema file> <destination>
```

See also [Compiling the schema](http://avro.apache.org/docs/current/gettingstartedjava.html#Compiling+the+schema). You can also integrate the schema compilation using the [avro-maven-plugin](http://avro.apache.org/docs/current/gettingstartedjava.html).

## Owner verifier implementation

All Kaa owner verifiers extend generic abstract class `org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier`.
The following code example illustrates implementation of a custom owner verifier.

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

An owner verifier descriptor provides Kaa with the information on how to locate and configure your custom owner verifier.
To implement an owner verifier descriptor, implement the `PluginConfig` interface first.

It is also important to provide your class with the `@KaaPluginConfig` annotation. This annotation helps Kaa Administration UI to find all available owner verifiers in the class path.

>**NOTE:** If you are going to configure your owner verifiers using REST API only, the owner verifier descriptor is optional.
{:.note}

The following code example illustrates the implementation of an owner verifier descriptor.

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

To provision your owner verifier:

1. Create a maven project.
You can use this [pom]({{github_url}}server/verifiers/trustful-verifier/pom.xml) as an example.

2. Create similar classes as defined above and put them in appropriate packages.

3. Add your verification logic to methods of `CustomOwnerVerifier` and build your project using the command below.

		$ mvn clean install

4. Move the `*.jar` file containing the owner verifier to the `/usr/lib/kaa-node/lib` folder.

5. If you use other package than `org.kaaproject.kaa.server.verifiers.*`, edit the `kaa-node.properties` file in the `/usr/lib/kaa-node/conf` folder.
Use the parameter `additional_plugins_scan_package` to specify additional package to scan for Kaa plugins configuration.
In this case, it is `org.kaaproject.kaa.sample`.

6. Restart the `kaa-node` service.

		$ sudo service kaa-node restart

7. Use the [Administration UI]({{root_url}}Glossary/#administration-ui) or [REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Verifiers/editUserVerifier) to create/update/delete your owner verifier instances.
