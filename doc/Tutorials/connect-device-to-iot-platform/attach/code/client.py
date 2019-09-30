import argparse
import json
import logging
import paho.mqtt.client as mqtt
import random
import string
import time

DEFAULT_KPC_HOST = ""  # Platform host goes here
DEFAULT_KPC_PORT = 8883  # Platform port goes here

EPMX_INSTANCE_NAME = "epmx"

APPLICATION_NAME = "demo_application"
APPLICATION_VERSION = "demo_application_v1"

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


# Returns hard-coded metadata
def composeMetadata():
  return json.dumps(
    {
      "model": "MySmartMeter A300",
      "mac": "00-14-22-01-23-45"
    }
  )


def connectToServer(client, host, port):
  logger.info("Connecting to KPC instance at {0}:{1}...".format(host, port))
  client.connect(host, port, 60)
  logger.info("Successfully connected")


def disconnectFromServer(client, host, port):
  logger.info("Disconnecting from server at {0}:{1}.".format(host, port))
  client.disconnect()
  logger.info("Successfully disconnected")


# Compose KP1 topic for metadata
topic_metadata = "kp1/{application_version}/{service_instance}/{resource_path}".format(
  application_version=APPLICATION_VERSION,
  service_instance=EPMX_INSTANCE_NAME,
  resource_path="{token}/update/keys".format(token=token)
)
logger.debug("Composed metadata topic: {}".format(topic_metadata))

# Initiate server connection
client = mqtt.Client(client_id=client_id)
connectToServer(client=client, host=host, port=port)

# Send metadata once on the first connection
data = composeMetadata()
client.publish(topic=topic_metadata, payload=data)
logger.info("Sent metadata: {0}\n".format(data))
time.sleep(1)
# Disconnect from server
disconnectFromServer(client=client, host=host, port=port)
