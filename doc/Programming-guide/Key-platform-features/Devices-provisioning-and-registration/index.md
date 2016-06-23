---
layout: page
title: Devices provisioning and registration
permalink: /:path/
nav: /:path/Programming-guide/Key-platform-features/Devices-provisioning-and-registration/
sort_idx: 10
---

* TOC
{:toc}

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

Kaa establishes a mechanism to uniquely identify each endpoint and the associated tokens and credentials issued to that endpoint.
Before first connection to Kaa you should generate a key pair (private and public key) and send request for registration of public key in Kaa. 
For this use [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO).
Registration of public keys occurs only once. In such an approach, all message will be encrypted with a recipient's public key. 
The message cannot be decrypted by anyone who does not possess the matching private key, who is thus presumed to be the owner of that key and the endpoint associated with the public key. 
Also you can disable the access to server for endpoint by terminate all active sessions with this endpoint. All subsequent requests will be forbidden.
For this use [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO).
In case where you need to bind specified endpoint profile to the credentials, use appropriate method in [Admin REST API]({{root_url}}Programming-guide/Server-REST-APIs/#TODO). 

## Endpoint Credentials



## Credentials service

Credential is an object that is verified when presented to the verifier in an authentication transaction. 
It establish the identity of endpoint. 

Kaa provides two credentials service

1. Trustful credentials service -  allows any endpoint to register and connect to Kaa cluster. (like the previous version of Kaa)
2. Internal credentials service -  allows connecting with Kaa for specified list of endpoints whose credentials was previously provisioned with Kaa REST API.

When Tenant Admin create application he can specify credential service for this application. By default used "Trustful".

## Creating custom credentials service

1. First of all you should create class which implements all method of 
[CredentialsService interface](https://github.com/kaaproject/kaa/blob/1d429a30bb4b5206376b740bb21483929a881ace/server/node/src/main/java/org/kaaproject/kaa/server/node/service/credentials/CredentialsService.java)

```java

package org.myproject;

@Service
public class CustomCredentialsService implements CredentialsService {

    /**
     * Provide credentials information to the external system.
     */
    @Override
    public CredentialsDto provideCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException {
        // implementation
    }

    /**
     * Returns the credentials by ID.
     */
    @Override
    public Optional<CredentialsDto> lookupCredentials(String applicationId, String credentialsId) throws CredentialsServiceException {
        // implementation
    }

    /**
     * Sets the status of the given credentials to CredentialsStatus.IN_USE
     */
    @Override
    public void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException {
        // implementation
    }

    /**
     * Revokes the given credentials by setting their status to CredentialsStatus.REVOKED
     */
    @Override
    public void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException {
        // implementation
    }
}

```

Exist three credential status

* AVAILABLE
* IN_USE
* REVOKED

2. In in /usr/lib/kaa-node/conf/kaaNodeContext.xml register CredentialsServiceLocator for your new credential service 

```xml

<bean id="customCredentialsServiceLocator" class="org.kaaproject.kaa.server.node.service.credentials.InternalCredentialsServiceLocator">
    <constructor-arg ref="org.myproject.customCredentialsService"/>
</bean>
    
<util:map id="credentialsServiceLocatorMap">
    <entry key="Trustful" value-ref="trustfulCredentialsServiceLocator"/>
    <entry key="Internal" value-ref="internalCredentialsServiceLocator"/>
    <entry key="NEW" value-ref="customCredentialsServiceLocator"/>                          
</util:map>

```

Value of key which was added you will see in Admin UI.

![credential](credential.png)

3. In in /usr/lib/kaa-node/conf/common-dao-context.xml

```xml

<bean id="customCredentialsService" class="org.myproject.CustomCredentialsService"/>
<alias name="customCredentialsService" alias="NEW Credentials Service"/>

```

##  Custom credentials service provisioning

To provision your credentials service, do the following:

1. Build your application using next command: 

   ```
      $ mvn clean install
   ```

2. Place ```*.jar``` of your application from ```/target``` folder into the ```/usr/lib/kaa-node/lib``` folder.
3. If you using different package than ```org.kaaproject.kaa.*```, you need to specify it to scan in ```kaa-node.properties``` file in ```/usr/lib/kaa-node/conf``` folder.

    For example provided in this article:
    
   ```additional_plugins_scan_package=org.myproject```.

4. Restart kaa-node service: 

   ```bash
      $ sudo service kaa-node restart
   ```