import argparse
import json
import logging
import random
import signal
import string
import sys
import time

import paho.mqtt.client as mqtt

DEFAULT_KPC_HOST = ""     # Platform host goes here
DEFAULT_KPC_PORT = 8883   # Platform port goes here

DCX_INSTANCE_NAME = "dcx"

APPLICATION_NAME = "demo_application"
APPLICATION_VERSION = "demo_application_v1"


def load_json(path):
  with open(path) as json_file:
    return json.load(json_file)


def killhandle(signum, frame):
  logger.info("SIGTERM detected, shutting down")
  disconnect_from_server(client=client, host=host, port=port)
  sys.exit(0)


signal.signal(signal.SIGINT, killhandle)
signal.signal(signal.SIGTERM, killhandle)

# Configure logging
logger = logging.getLogger('mqtt-client')
logger.setLevel(logging.DEBUG)

hdl = logging.StreamHandler()
hdl.setLevel(logging.DEBUG)
hdl.setFormatter(logging.Formatter('%(levelname)s: %(message)s'))

logger.addHandler(hdl)

# Parse command line arguments
parser = argparse.ArgumentParser(description="MQTT client for demo application")
parser.add_argument("-t", "--token", action="store", dest="token", required=True, help="Device token")
parser.add_argument("-s", "--host", action="store", dest="host", default=DEFAULT_KPC_HOST, help="Server host to connect to")
parser.add_argument("-p", "--port", action="store", dest="port", default=DEFAULT_KPC_PORT, help="Server port to connect to")
args = parser.parse_args()
token = args.token
client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
host = args.host
port = args.port
logger.info("Using EP token {0}, server at {1}:{2}".format(token, host, port))


def connect_to_server(client, host, port):
  logger.info("Connecting to KPC instance at {0}:{1}...".format(host, port))
  client.connect(host, port, 60)
  logger.info("Successfully connected")


def disconnect_from_server(client, host, port):
  logger.info("Disconnecting from server at {0}:{1}.".format(host, port))
  time.sleep(4)  # wait
  client.loop_stop()  # stop the loop
  client.disconnect()
  logger.info("Successfully disconnected")


# TELEMETRY section --------------------------------------
# Compose KP1 topic for data collection
data_request_id = random.randint(1, 99)
topic_data_collection = "kp1/{application_version}/{service_instance}/{resource_path}".format(
  application_version=APPLICATION_VERSION,
  service_instance=DCX_INSTANCE_NAME,
  resource_path="{token}/json/{data_request_id}".format(token=token, data_request_id=data_request_id)
)
logger.debug("Composed data collection topic: {}".format(topic_data_collection))


def compose_data_sample(location, fuelLevel):
  payload = [
    {
      "timestamp": int(round(time.time() * 1000)),
      "temperature": random.randint(95, 100),
      "latitude": location[0],
      "longitude": location[1],
      "fuelLevel": fuelLevel,
    }
  ]
  return json.dumps(payload)


def on_message(client, userdata, message):
  logger.info("Message received: topic [{}]\nbody [{}]".format(message.topic, str(message.payload.decode("utf-8"))))


# Initiate server connection
client = mqtt.Client(client_id=client_id)
client.on_message = on_message
connect_to_server(client=client, host=host, port=port)
# Start the loop
client.loop_start()

locationIndex = 0
fuelLevel = 100
# Send data sample in loop
while 1:
  location = load_json("./location.json")
  if locationIndex == len(location) - 1:
    locationIndex = 0
  if fuelLevel < 1:
    fuelLevel = 100

  payload = compose_data_sample(location[locationIndex], fuelLevel)
  result = client.publish(topic=topic_data_collection, payload=payload)

  if result.rc != 0:
    logger.info("Server connection lost, attempting to reconnect")
    connect_to_server(client=client, host=host, port=port)
  else:
    logger.debug("{0}: Sent next data: {1}".format(token, payload))

  time.sleep(3)
  locationIndex = locationIndex + 1
  fuelLevel = fuelLevel - 0.3
