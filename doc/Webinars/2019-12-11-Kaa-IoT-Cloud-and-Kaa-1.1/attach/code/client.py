import argparse
import json
import logging
import os
import random
import signal
import string
import sys
import time

import paho.mqtt.client as mqtt
import serial

# See https://bitbucket.org/MattHawkinsUK/rpispy-misc/raw/master/python/bme280.py
import bme280

DEFAULT_KPC_HOST = os.getenv('DEFAULT_KPC_HOST', 'cloud.kaaiot.com')
DEFAULT_KPC_PORT = os.getenv('DEFAULT_KPC_PORT', 1883)

EPMX_INSTANCE_NAME = os.getenv('EPMX_INSTANCE_NAME', 'epmx')
DCX_INSTANCE_NAME = os.getenv('DCX_INSTANCE_NAME', 'dcx')

# S08 CO2 sensor
ser = serial.Serial("/dev/ttyS0", baudrate=9600, timeout=.5)
ser.flushInput()
time.sleep(1)


def getCo2():
  ser.flushInput()
  ser.write("\xFE\x44\x00\x08\x02\x9F\x25")
  time.sleep(1.9)
  resp = ser.read(7)
  high = ord(resp[3])
  low = ord(resp[4])
  co2 = (high * 256) + low
  time.sleep(.1)
  return co2


def killhandle(signum, frame):
  logger.info("SIGTERM detected, shutting down")
  disconnectFromServer(client=client, host=host, port=port)
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

# Parse command line arguments and get device name
parser = argparse.ArgumentParser(description="MQTT client for demo application")
parser.add_argument("-d", "--deviceName", action="store", dest="deviceName", default="Senseair S8 LP BME/BMP 280",
                    required=False, help="Name of connected device")
parser.add_argument("-a", "--appversion", action="store", dest="appversion", required=True,
                    help="Application version")
parser.add_argument("-t", "--token", action="store", dest="token", required=True,
                    help="Device token")
parser.add_argument("-s", "--host", action="store", dest="host", default=DEFAULT_KPC_HOST,
                    help="Server host to connect to")
parser.add_argument("-p", "--port", action="store", dest="port", default=DEFAULT_KPC_PORT,
                    help="Server port to connect to")
args = parser.parse_args()
appversion = args.appversion
token = args.token
client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
host = args.host
port = args.port
logger.info("Using EP token {0}, server at {1}:{2}".format(token, host, port))


def connectToServer(client, host, port):
  logger.info("Connecting to KPC instance at {0}:{1}...".format(host, port))
  client.connect(host, port, 60)
  logger.info("Successfully connected")


def disconnectFromServer(client, host, port):
  logger.info("Disconnecting from server at {0}:{1}.".format(host, port))
  time.sleep(4)  # wait
  client.loop_stop()  # stop the loop
  client.disconnect()
  logger.info("Successfully disconnected")


# METADATA section ------------------------------------
# Compose KP1 topic for metadata
metadata_request_id = random.randint(1, 99)
topic_metadata = "kp1/{application_version}/{service_instance}/{resource_path}".format(
  application_version=appversion,
  service_instance=EPMX_INSTANCE_NAME,
  resource_path="{token}/update/keys/{metadata_request_id}".format(token=token,
                                                                   metadata_request_id=metadata_request_id)
)
logger.debug("Composed metadata topic: {}".format(topic_metadata))


def composeMetadata(version):
  return json.dumps(
    {
      "model": "Senseair S8 LP BME/BMP 280",
      "fwVersion": version,
      "customer": "Andrew",
      "latitude": 40.71427,
      "longitude": -74.00597,
    }
  )


# TELEMETRY section --------------------------------------
# Compose KP1 topic for data collection
data_request_id = random.randint(1, 99)
topic_data_collection = "kp1/{application_version}/{service_instance}/{resource_path}".format(
  application_version=appversion,
  service_instance=DCX_INSTANCE_NAME,
  resource_path="{token}/json/{data_request_id}".format(token=token, data_request_id=data_request_id)
)
logger.debug("Composed data collection topic: {}".format(topic_data_collection))


def composeDataSample():
  # BME/BMP 280
  temperature, pressure, humidity = bme280.readBME280All()
  # Senseair S8 LP
  co2 = getCo2()

  payload = [
    {
      "timestamp": int(round(time.time() * 1000)),
      "temperature": round(temperature,1),
      "humidity": int(humidity),
      "pressure": round(pressure,2),
      "co2": co2
    }
  ]
  return json.dumps(payload)


def on_connect(client, userdata, flags, rc):
  if rc == 0:
    client.connected_flag = True  # set flag
    logger.info("Successfully connected to MQTT server")
  else:
    logger.info("Failed to connect to MQTT code. Returned code=", rc)


def on_message(client, userdata, message):
  logger.info("Message received: topic [{}]\nbody [{}]".format(message.topic, str(message.payload.decode("utf-8"))))


# Initiate server connection
client = mqtt.Client(client_id=client_id)
client.connected_flag = False  # create flag in class
client.on_connect = on_connect  # bind call back function

client.on_message = on_message
# Start the loop
client.loop_start()

connectToServer(client=client, host=host, port=int(port))

while not client.connected_flag:  # wait in loop
  logger.info("Waiting for connection with MQTT server")
  time.sleep(1)

# Send metadata once on the first connection
metadata = composeMetadata(version="v0.0.1")
client.publish(topic=topic_metadata, payload=metadata)
logger.info("Sent metadata: {0}\n".format(metadata))

# Send data sample in loop
while 1:
  payload = composeDataSample()
  result = client.publish(topic=topic_data_collection, payload=payload)

  if result.rc != 0:
    logger.info("Server connection lost, attempting to reconnect")
    connectToServer(client=client, host=host, port=port)
  else:
    logger.debug("{0}: Sent next data: {1}".format(token, payload))

  time.sleep(8)
