---
layout: page
title: Endpoint groups
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

In this section, you can learn how to create [endpoint groups]({{root_url}}Glossary/#endpoint-group).

Endpoint grouping is a Kaa feature that allows you to aggregate your [endpoints]({{root_url}}Glossary/#endpoint-ep) into endpoint groups within an [application]({{root_url}}Glossary/#kaa-application).
The membership of an endpoint in a group is based on matching the [endpoint profile]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) to the [profile filter]({{root_url}}Glossary/#profile-filter) assigned to that group.
This means that those endpoints whose profiles match the profile filters of a specific endpoint group become automatically registered as members of that group.
Endpoint group is an independently managed entity defined by the profile filter assigned to it.

Profile filters are predicate expressions that define characteristics of group members (endpoints).
These filters are executed against the endpoint profile to figure out whether or not the endpoint belongs to the group.

You can create unlimited number of groups.
Any endpoint can be a member of unlimited number of groups at the same time.

## Prerequisites

To use the examples below, you need to first set up either a [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox), or a [single Kaa node]({{root_url}}Administration-guide/System-installation/Single-node-installation/), or a full-blown [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).
After that, you need to create a tenant, tenant admin, application, and user (if you use Sandbox, you don't need to create a tenant and application).
To do this, you can use the [tenant]({{root_url}}Programming-guide/Server-REST-APIs/#/Tenant), [user]({{root_url}}Programming-guide/Server-REST-APIs/#/User) and [application]({{root_url}}Programming-guide/Server-REST-APIs/#/Application) APIs.

It is strongly recommended that you first read the [Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) section before you proceed.

## Profile filters

Profile filters in Kaa are based on the [Spring Expression Language](http://docs.spring.io/spring/docs/3.0.x/reference/expressions.html) (SpEL).
All filters must be specified as predicates (statements that may be either true or false).

Profile filters are evaluated using the following context variables:

* cp --- Client-side endpoint profile
* sp --- Server-side endpoint profile
* ekh ---Endpoint key hash

>**NOTE**: Different profile schema versions may require separate profile filters due to the schema structural differences.
>In case a group has no filter assigned for a specific profile schema version, the group will not apply to the endpoints that use the profile of this schema version.
{:.note}

### Profile filter examples

The following example illustrates the general idea of profile filters.

<ol>
<li markdown="1">
Let's assume we have the following client-side profile schema.

```json
[  
   {  
      "name":"ClientSideEndpointProfileChild",
      "namespace":"org.kaaproject.kaa.common.endpoint.gen",
      "type":"record",
      "fields":[  
         {  
            "name":"otherSimpleField",
            "type":"int"
         },
         {  
            "name":"stringField",
            "type":"string"
         }
      ]
   },
   {  
      "namespace":"org.kaaproject.kaa.common.endpoint.gen",
      "type":"record",
      "name":"ClientSideEndpointProfile",
      "fields":[  
         {  
            "name":"simpleField",
            "type":"string"
         },
         {  
            "name":"recordField",
            "type":"org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild"
         },
         {  
            "name":"arraySimpleField",
            "type":{  
               "type":"array",
               "items":"string"
            }
         },
         {  
            "name":"arrayRecordField",
            "type":{  
               "type":"array",
               "items":"org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild"
            }
         },
         {  
            "name":"nullableRecordField",
            "type":[  
               "org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild",
               "null"
            ]
         }
      ]
   }
]
```
</li>
<li markdown="1">
Let's assume we have the following server-side profile schema.
For the sake of example, we will make it a fairly simple schema.

```json
[  
   {  
      "namespace":"org.kaaproject.kaa.common.endpoint.gen",
      "type":"record",
      "name":"ServerSideEndpointProfile",
      "fields":[  
         {  
            "name":"simpleField",
            "type":"string"
         },
         {  
            "name":"arraySimpleField",
            "type":{  
               "type":"array",
               "items":"string"
            }
         }
      ]
   }
]
```
</li>
<li markdown="1">
Let's have the following client-side endpoint profile to complement our client-side schema.

```json
{  
   "simpleField":"CLIENT_SIDE_SIMPLE_FIELD",
   "recordField":{  
      "otherSimpleField":123,
      "stringField":"STRING_VALUE1"
   },
   "arraySimpleField":[  
      "CLIENT_SIDE_VALUE_1",
      "CLIENT_SIDE_VALUE_2"
   ],
   "arrayRecordField":[  
      {  
         "otherSimpleField":456,
         "stringField":"STRING_VALUE2"
      },
      {  
         "otherSimpleField":789,
         "stringField":"STRING_VALUE3"
      }
   ],
   "nullableRecordField":null
}
```
</li>
<li markdown="1">
Let's have the following server-side endpoint profile to complement our server-side schema.

```json
{  
   "simpleField":"SERVER_SIDE_SIMPLE_FIELD",
   "arraySimpleField":[  
      "SERVER_SIDE_VALUE_1",
      "SERVER_SIDE_VALUE_2"
   ]
}
```
</li>
</ol>

When you incorporated all the above schemas, the following filters will return **true** when applied to the given endpoint.

| Filter |Description| 
|-------------------------------------------------------------------------------------------------------------|
|#cp.simpleField=='CLIENT_SIDE_SIMPLE_FIELD' | The client-side endpoint profile contains <code>simpleField</code> whith the **SIMPLE_FIELD** value. |
|#sp.arraySimpleField[1]=='SERVER_SIDE_VALUE_2' | The server-side endpoint profile contains the <code>arraySimpleField</code> array that stores the **VALUE2** at index **1**.
|{'AAAAABBBBCCCDDD='}.contains(#ekh) | The endpoint key hash is **AAAAABBBBCCCDDD=** |
|#cp.arraySimpleField.size()==2 | The client-side endpoint profile contains the <code>arraySimpleField</code> array that stores two elements. |
|#cp.recordField.otherSimpleField==123 | The client-side endpoint profile contains the <code>recordField</code> record where <code>otherSimpleField</code> is set to **123**. |
|#cp.recordField.otherMapSimpleField.size()==2 | The client-side endpoint profile contains the <code>recordField</code> record in which the <code>otherMapSimpleField</code> collection contains two entries. |
|#cp.arrayRecordField[1].otherSimpleField==789 |The client-side endpoint profile contains the <code>arrayRecordField</code> array. This array stores an element at index **1** which is a record containing <code>otherSimpleField</code> set to **789**.|
|#cp.nullableRecordField==null |An example of how to check a field for the **null** value.|
|#cp.arraySimpleField[0]=='CLIENT_SIDE_VALUE_1' and # sp.arraySimpleField[0]=='SERVER_SIDE_VALUE_1'|An example of how to combine several conditions in a query.|
|!#arrayRecordField.?[otherSimpleField==456].isEmpty() |The <code>arrayRecordField</code> field is an array of records. It stores at least one element that contains <code>otherSimpleField</code> set to a value.| 

## Using endpoint groups

Every Kaa application, when created, becomes a member of the default group [all]({{root_url}}Glossary/#group-all).
This default group is created for every application and cannot be edited by users.

Every group has a *weight* that represents the group priority.
Higher weight number corresponds to higher priority.
The weight of the group **all** is **0**, which is the lowest priority.

The group **all** also has the following attributes:

* Name
* Weight
* Description
* [Profile filters](#profile-filters)
* [Configurations]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/#configuration-schema)
* [Notification topics]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/#notification-topics)

The associated profile filter is automatically set equal to **true** for each profile schema version in the system.
Therefore, the group **all** contains every endpoint registered in the application.
You can create your custom endpoint groups using the [Administration UI](#adding-endpoint-groups) or [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#!/Grouping/editEndpointGroup).

>**NOTE**: Once created, an endpoint group does not contain any endpoints, so you will need to create and add custom profile filters to the group.
{:.note}

You can assign multiple filters to a group.
Every profile filter is specific to one combination of client-side and server-side profile schema version.
Only one profile filter can be defined for a profile schema version combination.
However, you can also define profile filters that are not specific to neither client-side nor server-side profile part.
In this case, either client-side profile or server-side profile part will not be accessible in the filter.
This is useful in case you want to specify an endpoint group that is based on certain client-side profile properties and is not affected by the server-side profile updates and the other way around.

Below are examples of client-server schema combinations.

Client-side endpoint profile A.

```json
{ 
    "id":"device1",
    "os":"Android",
    "os_version":"2.2",
    "build":"2.0.1"
}
```
Server-side endpoint profile A.

```json
{ 
    "subscriptionPlan": "Regular",
    "activationFlag": "true"
}
```
Client-side endpoint profile B.

```json
{ 
    "id":"device2",
    "os":"Android",
    "os_version":"4.0.1",
    "build":"3.0 RC1"
}
```
Server-side endpoint profile B.

```json
{ 
    "subscriptionPlan": "Regular",
    "activationFlag": "false"
}
```

Client-side endpoint profile C.

```json
{ 
    "id":"device3",
    "os":"iOS",
    "os_version":"8.0.1",
    "build":"3.0 RC1"
}
```
Server-side endpoint profile C.

```json
{ 
    "subscriptionPlan": "Premium",
    "activationFlag": "true"
}
```

>**NOTE**: Once a profile filter is created, you need to activate it.
>Filters that are not activated do not affect any endpoint groups or endpoints.
>See [Adding profile filters](#adding-profile-filters).

## Custom endpoint groups

The table below demonstrates the use of profile filters and filtering results for sample profiles.

| Group name                                |Filter                                       | result for profile A   | result for profile B   | result for profile C |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|Android Froyo                              | endpoints	#cp.os.toString().equals("Android") and #cp.os_version.toString().startsWith("2.2")	|true    |false	|false |
|Android endpoints                          |#cp.os.toString().equals("Android")                                                            |true    |true	|false |
|iOS 8 endpoints	                        | #cp.os.toString().equals("iOS") and #cp.os_version.toString().startsWith("8")                 |false	 |false	|true  |
|iOS 8 endpoints	                        | #cp.os.toString().equals("iOS") and #cp.os_version.toString().startsWith("8")                 |false	 |false	|true  |
|3.0 RC1 QA group endpoints                 |#cp.build.toString().equals("3.0 RC1")                                                         |false	 |true	|true  |
|Deactivated devices                        | # sp.activationFlag == false                                                                  |false	 |true  |false |
|iOS devices with premium subscription plan | #cp.os.toString().equals("iOS") and #sp.subscriptionPlan.toString().equals("Premium")         |false	 |false |true  |

## Adding endpoint groups

Endpoint groups are created based on the profile filters.

To add a new endpoint group:

1. Under the **Schemas** section of the application, click **Endpoint groups**, then click **Add endpoint group**.

	![endpoint-groups](admin-ui/endpoint-groups.png "endpoint-groups")

2. On the **Add endpoint group** page, fill in the required fields and click **Add**.

	![create-endpoint-group](admin-ui/create-endpoint-group.png "create-endpoint-group")

3. On the **Endpoint group** page, add profile filters, configurations, and notification topics to the group, if necessary.

	![add-profile-filters-to-group](admin-ui/add-profile-filters-to-group.png "add-profile-filters-to-group")

### Adding profile filters

To add a profile filter for an endpoint group:

1. Under the **Schemas** section of the application, click **Endpoint groups**, then select the required group by clicking on the corresponding row in the list.

2. On the **Endpoint group** page, click **Add profile filter**.

3. On the **Profile filter** page, select the schema version.

4. Switch to the **Draft** tab, enter a description (optional) and enter the filter conditions in the **Filter body** section.

    ![profile-filter-details](admin-ui/profile-filter-details.png "profile-filter-details.png")

    >**TIP**: To test profile filter, click **Test filter**.
    >The **Test profile filter** menu will open.
    >Complete the endpoint and/or server profile forms and click **Test filter**.
    {:.tip}
    
    ![test-profile-filter](admin-ui/test-profile-filter.png "test-profile-filter.png")
    
5. Click **Save** to save the profile filter.

	>**TIP**: You can save your data on the **Draft** tab and return to update it later as many times as needed until you click **Activate**.
	{:.tip}
	
6. Click **Activate** to activate the profile filter.
   The profile filter information you entered is now visible in the **Active** tab.

## Using REST API

In alternative to using the Administration UI, you can use the [server REST API]({{root_url}}Programming-guide/Server-REST-APIs/#/Profiling) to perform the above actions.

## Further reading

* [Spring Expression Language](http://docs.spring.io/spring/docs/3.0.x/reference/expressions.html)
* [Avro](http://avro.apache.org/)


