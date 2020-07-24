import json
import logging
import random
import string
import time

import paho.mqtt.client as mqtt

KPC_HOST = "mqtt.cloud.kaaiot.com"
KPC_PORT = 1883

ENDPOINT_TOKEN = ""  # Paste endpoint token
APPLICATION_VERSION = ""  # Paste application version

# Configure logging
logger = logging.getLogger('mqtt-client')
logger.setLevel(logging.DEBUG)

hdl = logging.StreamHandler()
hdl.setLevel(logging.DEBUG)
hdl.setFormatter(logging.Formatter('%(levelname)s: %(message)s'))

logger.addHandler(hdl)

logger.info(f'Using endpoint token {ENDPOINT_TOKEN}, server at {KPC_HOST}:{KPC_PORT}')


# Returns hard-coded metadata
def get_metadata():
  return json.dumps(
    {
      "model": "MySmartMeter A300",
      "mac": "00-14-22-01-23-45"
    }
  )


def connect_to_server(client):
  logger.info(f'Connecting to KPC instance at {KPC_HOST}:{KPC_PORT}...')
  client.connect(KPC_HOST, KPC_PORT, 60)
  logger.info("Successfully connected")


def disconnect_from_server(client):
  logger.info('Disconnecting from server')
  client.disconnect()
  logger.info("Successfully disconnected")


def log_metadata(client, userdata, message):
  logger.info(f'Received metadata from server: {str(message.payload.decode("utf-8"))} on topic: {message.topic}')


def log_metadata_keys(client, userdata, message):
  logger.info(f'Received metadata keys from server: {str(message.payload.decode("utf-8"))} on topic: {message.topic}')


def log_partial_metadata_update_response(client, userdata, message):
  logger.info(f'Received partial metadata update response: {str(message.payload.decode("utf-8"))} on topic: {message.topic}')


request_id = random.randint(0, 99)

# MQTT PUBLISH topic: "get metadata" request
get_metadata_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/get/{request_id}'
logger.info(f'{get_metadata_publish_topic} - MQTT PUBLISH topic: "get metadata" request')

# MQTT SUBSCRIBE topic: "get metadata" response
get_metadata_subscribe_topic = f'{get_metadata_publish_topic}/status'
logger.info(f'{get_metadata_subscribe_topic} - MQTT SUBSCRIBE topic: "get metadata" response')

# MQTT PUBLISH topic: "get metadata keys" request
get_metadata_keys_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/get/keys/{request_id}'
logger.info(f'{get_metadata_keys_publish_topic} - MQTT PUBLISH topic: "get metadata keys" request')

# MQTT SUBSCRIBE topic: "get metadata keys" response
get_metadata_keys_subscribe_topic = f'{get_metadata_keys_publish_topic}/status'
logger.info(
  f'{get_metadata_keys_subscribe_topic} - MQTT SUBSCRIBE topic: "get metadata keys" response')

# MQTT PUBLISH topic: "partial metadata update" request
partial_metadata_udpate_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/update/keys/{request_id}'
logger.info(
  f'{partial_metadata_udpate_publish_topic} - MQTT PUBLISH topic: "partial metadata update"')

# MQTT SUBSCRIBE topic: "partial metadata update" response
partial_metadata_update_subscribe_topic = f'{partial_metadata_udpate_publish_topic}/status'
logger.info(
  f'{partial_metadata_update_subscribe_topic} - MQTT SUBSCRIBE topic: "partial metadata update"')

# MQTT PUBLISH topic: "full metadata update"
full_metadata_udpate_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/update'
logger.info(f'{full_metadata_udpate_publish_topic} - MQTT PUBLISH topic: "full metadata update"')

# MQTT PUBLISH topic: "delete metadata keys"
delete_metadata_keys_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/delete/keys'
logger.info(f'{delete_metadata_keys_publish_topic} - MQTT PUBLISH topic: "delete metadata keys"')


def on_message(client, userdata, message):
  logger.info(f'Message received: topic: {message.topic}\nbody: {str(message.payload.decode("utf-8"))}')


def on_connect(client, userdata, flags, rc):
  logger.info("Requesting metadata from server")
  client.publish(topic=get_metadata_publish_topic, payload=json.dumps({}))
  client.publish(topic=get_metadata_keys_publish_topic, payload=json.dumps([]))


# Initiate server connection
client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
client = mqtt.Client(client_id=client_id)
client.on_message = on_message
client.on_connect = on_connect
connect_to_server(client=client)
# Start the loop
client.loop_start()

client.message_callback_add(get_metadata_subscribe_topic, log_metadata)
client.message_callback_add(get_metadata_keys_subscribe_topic, log_metadata_keys)
client.message_callback_add(partial_metadata_update_subscribe_topic,
                            log_partial_metadata_update_response)

# Send metadata
data = get_metadata()
client.publish(topic=partial_metadata_udpate_publish_topic, payload=data)
logger.info(f'Sent metadata: {data}')
time.sleep(5)
disconnect_from_server(client=client)
