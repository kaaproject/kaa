import json
import random
import string
import time

import paho.mqtt.client as mqtt

KPC_HOST = "mqtt.cloud.kaaiot.com"
KPC_PORT = 1883

ENDPOINT_TOKEN = ""       # Paste endpoint token
APPLICATION_VERSION = ""  # Paste application version

print(f'Using endpoint token {ENDPOINT_TOKEN}, server at {KPC_HOST}:{KPC_PORT}')

request_id = random.randint(0, 99)

# MQTT PUBLISH topic: "get metadata" request
get_metadata_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/get/{request_id}'
print(f'{get_metadata_publish_topic} - MQTT PUBLISH topic: "get metadata" request')

# MQTT SUBSCRIBE topic: "get metadata" response
get_metadata_subscribe_topic = f'{get_metadata_publish_topic}/status'
print(f'{get_metadata_subscribe_topic} - MQTT SUBSCRIBE topic: "get metadata" response')

# MQTT PUBLISH topic: "partial metadata update" request
partial_metadata_udpate_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/update/keys/{request_id}'
print(f'{partial_metadata_udpate_publish_topic} - MQTT PUBLISH topic: "partial metadata update"')

# MQTT SUBSCRIBE topic: "partial metadata update" response
partial_metadata_update_subscribe_topic = f'{partial_metadata_udpate_publish_topic}/status'
print(f'{partial_metadata_update_subscribe_topic} - MQTT SUBSCRIBE topic: "partial metadata update"')

# MQTT PUBLISH topic: "full metadata update"
full_metadata_udpate_publish_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/update'
print(f'{full_metadata_udpate_publish_topic} - MQTT PUBLISH topic: "full metadata update"')


# Returns hard-coded metadata
def get_metadata():
  return json.dumps(
    {
      "model": "MySmartMeter A300",
      "mac": "00-14-22-01-23-45"
    }
  )

def log_metadata(client, userdata, message):
  print(f'Received metadata from server: {str(message.payload.decode("utf-8"))} on topic: {message.topic}')


def log_partial_metadata_update_response(client, userdata, message):
  print(f'Received partial metadata update response: {str(message.payload.decode("utf-8"))} on topic: {message.topic}')


def on_message(client, userdata, message):
  print(f'Message received: topic: {message.topic}\nbody: {str(message.payload.decode("utf-8"))}')


def on_connect(client, userdata, flags, rc):
  print("Requesting metadata from server")
  # Request metadata
  client.publish(topic=get_metadata_publish_topic, payload=json.dumps({}))

def main():
  # Initiate server connection
  client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
  client = mqtt.Client(client_id=client_id)
  client.on_message = on_message
  client.on_connect = on_connect
  client.connect(KPC_HOST, KPC_PORT, 60)
  client.loop_start()

  client.message_callback_add(get_metadata_subscribe_topic, log_metadata)
  client.message_callback_add(partial_metadata_update_subscribe_topic, log_partial_metadata_update_response)

  # Report metadata
  data = get_metadata()
  client.publish(topic=partial_metadata_udpate_publish_topic, payload=data)
  print(f'Reported metadata: {data}')
  time.sleep(5)
  client.disconnect()

if __name__ == '__main__':
  main()
