---
layout: page
title: Endpoint groups management
permalink: /:path/
nav: /:path/Programming-guide/Key-system-features/Endpoint-groups-management
sort_idx: 40
---


Kaa allows for aggregating endpoints related to the same application into endpoint groups. The endpoint group represents an independent managed entity which is defined by the profile filters assigned to the group. Those endpoints whose profiles match the profile filters of the specific endpoint group become automatically registered as members of this group. There is no restriction for endpoints on having membership in a number of groups at a time.
Endpoint group profile filters are predicate expressions which define characteristics of group members (endpoints). These filters are executed against the endpoint profile to figure out whether or not the endpoint belongs to the group.

### NOTE ###

	Different profile schema versions may require separate profile filters due to the schema structural differences. 
	In case a group has no filter assigned for a specific profile schema version, the group does not apply to the endpoints that use the profile of this schema version.

Every endpoint group within the application has a unique weight value assigned to it. The weight value is used to resolve conflicts among endpoint groups: in general, the larger the weight, the higher the group priority.
Every Kaa application comes with the default, non-user-editable group "all", with the weight value 0. The profile filter of this group is automatically set to "true" for every profile schema version in the system. As a result, the "all" group contains every endpoint registered in the application. The "all" group is used to define the default configuration, default notification topics access list, and for some other special functions.


## Profile filters ##


Profile filters in Kaa are based on the [Spring Expression Language](http://docs.spring.io/spring/docs/3.0.x/reference/expressions.html) (SpEL). All filters must be specified as predicates (statements which may be either true or false). For further reference on the filters syntax, please refer to the Spring documentation.
Profile filters are evaluated using following context variables: 


* "cp" - Client-side endpoint profile
* "sp" - Server-side endpoint profile
* "ekh" - Endpoint key hash

## Profile filter examples ##

The following example illustrates the general idea of profile filters.


* Let's assume the following client-side profile schema.


```json
[
    {
        "name": "ClientSideEndpointProfileChild",
        "namespace": "org.kaaproject.kaa.common.endpoint.gen",
        "type": "record",
        "fields": [
            {
                "name": "otherSimpleField",
                "type": "int"
            },
            {
                "name": "stringField",
                "type": "string"
            }
        ]
    },
    {
        "namespace": "org.kaaproject.kaa.common.endpoint.gen",
        "type": "record",
        "name": "ClientSideEndpointProfile",
        "fields": [
            {
                "name": "simpleField",
                "type": "string"
            },
            {
                "name": "recordField",
                "type": "org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild"
            },
            {
                "name": "arraySimpleField",
                "type": {
                    "type": "array",
                    "items": "string"
                }
            },
            {
                "name": "arrayRecordField",
                "type": {
                    "type": "array",
                    "items": "org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild"
                }
            },
            {
                "name": "nullableRecordField",
                "type": ["org.kaaproject.kaa.common.endpoint.gen.ClientSideEndpointProfileChild", "null"]
            }
        ]
    }
]
```


* Let's assume the following server-side profile schema. Please note that this schema is less complex only for demonstration purposes. Both Client-side and Server-side profile schemas support same level of complexity.


```json
[
    {
        "namespace": "org.kaaproject.kaa.common.endpoint.gen",
        "type": "record",
        "name": "ServerSideEndpointProfile",
        "fields": [
            {
                "name": "simpleField",
                "type": "string"
            },
            {
                "name": "arraySimpleField",
                "type": {
                    "type": "array",
                    "items": "string"
                }
            }
        ]
    }
]
```

* Second, let's assume the following client-side endpoint profile, which corresponds to the schema.


```json
{
    "simpleField": "CLIENT_SIDE_SIMPLE_FIELD",
    "recordField": {
        "otherSimpleField": 123,
        "stringField": "STRING_VALUE1"
    },
    "arraySimpleField": ["CLIENT_SIDE_VALUE_1", "CLIENT_SIDE_VALUE_2"],
    "arrayRecordField": [
        {
            "otherSimpleField": 456,
            "stringField": "STRING_VALUE2"
        },
        {
            "otherSimpleField": 789,
            "stringField": "STRING_VALUE3"
        }
    ],
    "nullableRecordField": null
}
```


* Let's assume the following server-side endpoint profile, which corresponds to the schema.


```json
{
    "simpleField": "SERVER_SIDE_SIMPLE_FIELD",
    "arraySimpleField": ["SERVER_SIDE_VALUE_1", "SERVER_SIDE_VALUE_2"]
}
```

At last, the following filters will yield true when applied to the given endpoint.


| Filter                                                                                           | Explanation                                                                                                                                                                                  |
|--------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| {'AAAAABBBBCCCDDD='}.contains(#endpointKeyHash)                                                  | The endpoint key hash is AAAAABBBBCCCDDD=                                                                                                                                                    |
| #sp.arraySimpleField[1]=='SERVER_SIDE_VALUE_2'                                                   | The server-side endpoint profile contains arraySimpleField, which is an array containing the element 'VALUE2' in the position 1.                                                             |
| #cp.simpleField=='CLIENT_SIDE_SIMPLE_FIELD'                                                      | The client-side endpoint profile contains simpleField with the value 'SIMPLE_FIELD'.                                                                                                         |
| #cp.recordField.otherSimpleField==123                                                            | The client-side endpoint profile contains recordField, which is a record containing otherSimpleField set to '123'.                                                                           |
| #cp.recordField.otherMapSimpleField.size()==2                                                    | The client-side endpoint profile contains recordField, which is a record containing otherMapSimpleField, which is a collection containing two entries.                                       |
| #cp.nullableRecordField==null                                                                    | An example of how to check a field for the null value.                                                                                                                                       |
| #cp.arraySimpleField[0]=='CLIENT_SIDE_VALUE_1' and#sp.arraySimpleField[0]=='SERVER_SIDE_VALUE_1' | An example of how to combine several conditions in a query.                                                                                                                                  |
| #cp.arraySimpleField.size()==2                                                                   | The client-side endpoint profile contains arraySimpleField, which is a collection containing two elements.                                                                                   |
| #cp.arrayRecordField[1].otherSimpleField==789                                                    | The client-side endpoint profile contains arrayRecordField, which is an array. This array contains an element in the position 1, which is a record containing otherSimpleField set to '789'. |
| !#arrayRecordField.?[otherSimpleField==456].isEmpty()                                            | The arrayRecordField field is an array of records. It contains at least one element that contains otherSimpleField with the value 456.                                                       |
