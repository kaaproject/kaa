---
layout: page
title: MQTT Topics
permalink: /:path/
sort_idx: 8
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}

## Data collection

### Send data samples

**PUB kp1/{appversion_name}/dcx/{token}/json[/{request ID}]**

Publish telemetry data to the platform in JSON format. Supports single and batched messages sent as an array. Historical data is stored in the two databases. For fast access of the raw data it's stored in the InfluxDB and accessible via EPTS service API and can be customized in EPTS settings. For the aggregated data it's stored in OpenSearch which works on top of ElasticSearch and can be accessed via ASF API or Kibana.

When used with MQTT optionally specify the "Request ID" to subscribe on /status or /error topic to get the operation status. Alternatively subscribe to "kp1/<appversion_name>/dcx/<token>/json/#" to receieve both status and error responses.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "2/DCP request schema",
    "type": "array"
}
```

Payload example for single data sample:

```json
 {
    "temperature": 21,
    "humidity": 73,
    "location": {
        "lat": 34.1340258,
        "lon": -118.3238652
    }
}
```

Batched payload with predefined timestamp and nested location object. Pay attention to the "ts" field that is timestamp for a sample and should be configured in EPTS service. "location" object should be configured as custom Time series to value mapping in EPTS settings. Analytics storage may require to specify the mappping to parse the string as a date and can be configured in Data transformation -> Mappings

Payload example for batch payload:

```json
[
    {
        "ts": "2019-04-26T15:41:07+0000",
        "temperature": 21,
        "humidity": 73,
        "location": {
            "lat": 34.1340258,
            "lon": -118.3238652
        }
    },
    {
        "ts": "2019-04-26T16:41:07+0000",
        "temperature": 21.5,
        "humidity": 67,
        "location": {
            "lat": 34.1340258,
            "lon": -118.3238652
        }
    }
]
```

**Subscribe status / error topics:**

- **SUB** kp1/bspu542ikfmmfgrk47s0-v1/dcx/<token>/json[/<request ID>]/status
- **SUB** kp1/bspu542ikfmmfgrk47s0-v1/dcx/<token>/json[/<request ID>]/error

### Send plain data samples

**PUB kp1/<appversion_name>/dcx/<token>/plain/<metric_name>**

Publish telemetry data to the platform in plain format. Optionally you can specify units in the value that will be mapped under ${metric-name}-unit key. **NOTE:** It's required to enable **Relaxed resource path validation** in [Data collection extension][DCX] service settings at the application level

Payload schema:

```json
{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": [
        "number",
        "string"
    ]
}
```

Payload example:

```json
200
```

## Metadata

### Get all metadata keys request

**PUB kp1/<appversion_name>/epmx/<token>/get/keys/<request ID>**

Publish the message to request all endpoint metadata keys. Response available at topics /status and error at /error ending relative to this topic. Alternatively subscribe to "kp1/<appversion_name>/epmx/<token>/get/keys/#" to receieve both status and error responses.

Response schema:

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "10/EPMP get metadata keys response: a set of endpoint metadata keys",
    "type": "array",
    "items": {
        "type": "string",
        "pattern": "^[a-zA-Z0-9_]+$",
        "description": "Endpoint metadata key name"
    },
    "uniqueItems": true
}
```

Response example:

```json
[
    "name",
    "location",
    "deviceModel"
]
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/epmx/<token>/get/keys/<request ID>/status
- **SUB** kp1/<appversion_name>/epmx/<token>/get/keys/<request ID>/error



### Get metadata request

**PUB kp1/<appversion_name>/epmx/<token>/get/<request ID>**

Publish the message to request all endpoint metadata. You need to subscribe to the response topic to get the result at /status ending relative to this topic.

Response schema

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "10/EPMP get metadata response: a set of EP metadata key-value pairs in JSON object representation",
    "type": "object",
    "patternProperties": {
        "^[a-zA-Z0-9_]+$": {}
    },
    "additionalProperties": false
}
```

Response example

```json
{
    "name": "Device name",
    "deviceModel": "new model"
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/epmx/<token>/get/<request ID>/status
- **SUB** kp1/<appversion_name>/epmx/<token>/get/<request ID>/error

### Full metadata update

**PUB kp1/<appversion_name>/epmx/<token>/update[/<request ID>]**

Update endpoint metadata with a set of key-value pairs in JSON object representation. When using with MQTT optionaliy sepcify the "Request ID" to subscribe on /status or /error topic to get the operation status.

Payload schema

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "10/EPMP update metadata request: a set of EP metadata key-value pairs in JSON object representation",
    "type": "object",
    "minProperties": 1,
    "patternProperties": {
        "^[a-zA-Z0-9_]+$": {}
    },
    "additionalProperties": false
}
```

Payload example

```json
{
    "deviceModel": "example model",
    "name": "Sensor 1"
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/epmx/<token>/update[/<request ID>]/status
- **SUB** kp1/<appversion_name>/epmx/<token>/update[/<request ID>]/error


### Partial metadata update

**PUB kp1/<appversion_name>/epmx/<token>/update/keys[/<request ID>]**

Update endpoint metadata with a set of key-value pairs in JSON object representation. When using with MQTT optionaliy sepcify the "Request ID" to subscribe on /status or /error topic to get the operation status.

Payload schema

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "10/EPMP update metadata request: a set of EP metadata key-value pairs in JSON object representation",
    "type": "object",
    "minProperties": 1,
    "patternProperties": {
        "^[a-zA-Z0-9_]+$": {}
    },
    "additionalProperties": false
}
```

Payload example

```json
{
    "deviceModel": "new model"
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/epmx/<token>/update/keys[/<request ID>]/status
- **SUB** kp1/<appversion_name>/epmx/<token>/update/keys[/<request ID>]/error

### Delete metadata keys

**PUB kp1/<appversion_name>/epmx/<token>/delete/keys[/<request ID>]**

Delete endpoint metadata keys. When using with MQTT optionaliy sepcify the "Request ID" to subscribe on /status or /error topic to get the operation status.

Payload schema

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "10/EPMP delete metadata keys request",
    "type": "array",
    "items": {
        "type": "string",
        "pattern": "^[a-zA-Z0-9_]+$",
        "description": "Endpoint metadata key name"
    },
    "minItems": 1,
    "uniqueItems": true
}
```

Payload example

```json
[
    "location",
    "areaId"
]
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/epmx/<token>/delete/keys[/<request ID>]/status
- **SUB** kp1/<appversion_name>/epmx/<token>/delete/keys[/<request ID>]/error

## Command execution

### Request pending commands
**PUB kp1/<appversion_name>/cex/<token>/command/<command_type>[/<request ID>]**

Request pending commands for the endpoint.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "11/CEP command request schema",
    "type": "object",
    "properties": {
        "observe": {
            "type": "boolean",
            "description": "Whether to send upcoming command for this endpoint."
        }
    },
    "additionalProperties": false
}
```

Payload example

```json
{
    "observe": true
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/cex/<token>/command/<command_type>[/<request ID>]/status
- **SUB** kp1/<appversion_name>/cex/<token>/command/<command_type>[/<request ID>]/error

Response schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "11/CEP command request schema",
    "type": "object",
    "properties": {
        "observe": {
            "type": "boolean",
            "description": "Whether to send upcoming command for this endpoint."
        }
    },
    "additionalProperties": false
}
```

Response example

```json
{
    "observe": true
}
```

### Report command execution result

**PUB kp1/<appversion_name>/cex/<token>/result/<command_type>[/<request ID>]**

Report command execution result to the platform. Optionally specify the "Request ID" to subscribe on /error topic to get the operation status.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "11/CEP result request schema",
    "type": "array",
    "items": {
        "type": "object",
        "properties": {
            "id": {
                "type": "integer",
                "description": "ID of the command."
            },
            "statusCode": {
                "type": "integer",
                "description": "Status code of the command execution. Based on HTTP status codes."
            },
            "reasonPhrase": {
                "type": "string",
                "description": "Intended to give a short textual description of the status code."
            },
            "payload": {
                "description": "A command result payload to be interpreted by the caller."
            }
        },
        "required": [
            "id",
            "statusCode"
        ],
        "additionalProperties": false
    }
}
```

Payload example

```json
[
    {
        "id": 1,
        "statusCode": 200,
        "reasonPhrase": "Ok",
        "payload": {
            "engine_on": true
        }
    }
]
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/cex/<token>/result/<command_type>[/<request ID>]/status
- **SUB** kp1/<appversion_name>/cex/<token>/result/<command_type>[/<request ID>]/error

## Configuration

### Configuration resource request

**PUB kp1/<appversion_name>/cmx/<token>/config/json/<request ID>**

Request configuration by ID. When using with MQTT you need to subscribe to the response topic to get the result at /status ending relative to this topic.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "7/CMP configuration request schema",
    "type": "object",
    "properties": {
        "configId": {
            "type": "string",
            "description": "Identifier of the currently applied configuration"
        },
        "observe": {
            "type": "boolean",
            "description": "Whether the endpoint is interested in observing its configuration"
        }
    },
    "additionalProperties": false
}
```

Payload example

```json
{}
```

Subscribe status / error topics:

- **SUB** kp1/<appversion_name>/cmx/<token>/config/json/<request ID>/status
- **SUB** kp1/<appversion_name>/cmx/<token>/config/json/<request ID>/error

Response schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "7/CMP configuration request schema",
    "type": "object",
    "properties": {
        "configId": {
            "type": "string",
            "description": "Identifier of the currently applied configuration"
        },
        "observe": {
            "type": "boolean",
            "description": "Whether the endpoint is interested in observing its configuration"
        }
    },
    "additionalProperties": false
}
```

Response example

```json
{
  "id": 42,
  "configId": "97016dbe8bb4adff8f754ecbf24612f2",
  "statusCode": 200,
  "reasonPhrase": "ok",
  "config": {
    "key": "value",
    "array": [
      "value2"
    ]
  }
}
```

### Report applied configuration request

**PUB kp1/<appversion_name>/cmx/<token>/applied/json/<request ID>**

Report applied configuration to the platform. Configuration can be rejected by sending the status code 400 or higher. When using MQTT you can subscribe to the response topic to get the result at /status ending relative to this topic.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "7/CMX applied configuration request schema",
    "type": "object",
    "properties": {
        "configId": {
            "type": "string",
            "description": "Identifier of the applied configuration"
        },
        "statusCode": {
            "type": "number",
            "description": "Status code based on HTTP status codes",
            "default": 200
        },
        "reasonPhrase": {
            "type": "string",
            "description": "Human-readable string explaining the cause of an error (if any)"
        }
    },
    "required": [
        "configId"
    ],
    "additionalProperties": false
}
```

Payload example

```json
{
    "configId": "97016dbe8bb4adff8f754ecbf24612f2"
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/cmx/<token>/applied/json/<request ID>/status
- **SUB** kp1/<appversion_name>/cmx/<token>/applied/json/<request ID>/error

## Software OTA

### Software resource request

**PUB kp1/<appversion_name>/cmx_ota/<token>/config/json/<request ID>**

Request the new firmware version for the endpoint. You need to subscribe to the response topic to get the result at /status ending relative to this topic.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "type": "object",
    "properties": {
        "observe": {
            "type": "boolean",
            "description": "Whether to send upcoming firmware updates for this endpoint."
        }
    },
    "additionalProperties": false
}
```

Payload example

```json
{
    "observe": true
}
```

Subscribe status / error topics:

- **SUB** kp1/<appversion_name>/cmx_ota/<token>/config/json/<request ID>/status
- **SUB** kp1/<appversion_name>/cmx_ota/<token>/config/json/<request ID>/error

Response schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "type": "object",
    "properties": {
        "observe": {
            "type": "boolean",
            "description": "Whether to send upcoming firmware updates for this endpoint."
        }
    },
    "additionalProperties": false
}
```

Response example

```json
{
    "observe": true
}
```

### Report applied software update request
**SUBkp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>
Report applied software update to the platform. Software update can be rejected by sending the status code 400 or higher. You need to subscribe to the response topic to get the result at /status ending relative to this topic.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "7/CMX applied configuration request schema",
    "type": "object",
    "properties": {
        "configId": {
            "type": "string",
            "description": "Identifier of the applied configuration"
        },
        "statusCode": {
            "type": "number",
            "description": "Status code based on HTTP status codes",
            "default": 200
        },
        "reasonPhrase": {
            "type": "string",
            "description": "Human-readable string explaining the cause of an error (if any)"
        }
    },
    "required": [
        "configId"
    ],
    "additionalProperties": false
}
```

Payload example

```json
{
    "configId": "0.0.2"
}
```

Subscribe status / error topics:

- **SUB** kp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>/status
- **SUB** kp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>/error


### Report applied software update request

**PUB kp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>**

Report applied software update to the platform. Software update can be rejected by sending the status code 400 or higher. You need to subscribe to the response topic to get the result at /status ending relative to this topic.

Payload schema

```json
{
    "$schema": "http://json-schema.org/schema#",
    "title": "7/CMX applied configuration request schema",
    "type": "object",
    "properties": {
        "configId": {
            "type": "string",
            "description": "Identifier of the applied configuration"
        },
        "statusCode": {
            "type": "number",
            "description": "Status code based on HTTP status codes",
            "default": 200
        },
        "reasonPhrase": {
            "type": "string",
            "description": "Human-readable string explaining the cause of an error (if any)"
        }
    },
    "required": [
        "configId"
    ],
    "additionalProperties": false
}
```

Payload example

```json
{
    "configId": "0.0.2"
}
```

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>/status
- **SUB** kp1/<appversion_name>/cmx_ota/<token>/applied/json/<request ID>/error

## Binary data upload

### Get upload token

**PUB kp1/<appversion_name>/bcx/<token>/token[/<request ID>]**

Request the upload token for the endpoint to be used in "x-auth-token" header for upload binary blob data.

**Subscribe status / error topics:**

- **SUB** kp1/<appversion_name>/bcx/<token>/token[/<request ID>]/status
- **SUB** kp1/<appversion_name>/bcx/<token>/token[/<request ID>]/error

Response schema

```json
{
    "type": "object",
    "properties": {
        "token": {
            "type": "string",
            "description": "Access token to be used in \"x-auth-token\" header for upload binary blob data."
        }
    },
    "additionalProperties": false
}
```

Response example

```json
{
    "token": "2222"
}
```