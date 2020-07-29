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

DEFAULT_KPC_HOST = "mqtt.cloud.kaaiot.com"  # Platform host goes here
DEFAULT_KPC_PORT = 1883  # Platform port goes here

DCX_INSTANCE_NAME = "dcx"
EPMX_INSTANCE_NAME = "epmx"
CEX_INSTANCE_NAME = "cex"

HEALTH_CHECK_COMMAND_TYPE = "HEALTH_CHECK"


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
parser.add_argument("-t", "--token", action="store", dest="token", required=True,
                    help="Device token")
parser.add_argument("-a", "--appVersionName", action="store", dest="appVersionName", required=True,
                    help="Application version name")
parser.add_argument("-s", "--host", action="store", dest="host", default=DEFAULT_KPC_HOST,
                    help="Server host to connect to")
parser.add_argument("-p", "--port", action="store", dest="port", default=DEFAULT_KPC_PORT,
                    help="Server port to connect to")
args = parser.parse_args()
appVersionName = args.appVersionName
token = args.token
client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
host = args.host
port = args.port
logger.info("Using endpoint token {0}, server at {1}:{2}".format(token, host, port))


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


# METADATA section --------------------------------------
# Compose KP1 topic for metadata
topic_metadata = "kp1/{application_version}/{service_instance}/{resource_path}".format(
    application_version=appVersionName,
    service_instance=EPMX_INSTANCE_NAME,
    resource_path="{token}/update/keys".format(token=token)
)
logger.debug("Composed metadata topic: {}".format(topic_metadata))


def compose_metadata():
    """
    Composes device metadata.
    :return: device metadata
    """
    return json.dumps(
        {
            "serial": "00-14-22-01-23-45",
            "mac": "50:8c:b1:77:e8:e6"
        }
    )


# TELEMETRY section --------------------------------------
# Compose KP1 topic for data collection
data_request_id = random.randint(1, 99)
topic_data_collection = "kp1/{application_version}/{service_instance}/{resource_path}".format(
    application_version=appVersionName,
    service_instance=DCX_INSTANCE_NAME,
    resource_path="{token}/json/{data_request_id}".format(token=token,
                                                          data_request_id=data_request_id)
)
logger.debug("Composed data collection topic: {}".format(topic_data_collection))


def compose_data_sample(location, battery_level):
    """
    Composes device telemetry data
    :return: device telemetry data
    """
    random_string = ''.join(
        random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
    data_sample = [
        {
            "timestamp": int(round(time.time() * 1000)),
            "temperature": random.randint(20, 25),
            "log": 'Randomly generated string: ' + random_string,
            "latitude": location[0],
            "longitude": location[1],
            "battery_level": round(battery_level),
        }
    ]
    return json.dumps(data_sample)


def compose_log_data_sample():
    data_sample = [
        {
            "log": "Endpoint health status: OK"
        }
    ]
    return json.dumps(data_sample)


# COMMAND section ----------------------------------------
# LOG COMMAND section ------------------------------------
topic_command_health_check = "kp1/{application_version}/{service_instance}/{resource_path}".format(
    application_version=appVersionName,
    service_instance=CEX_INSTANCE_NAME,
    resource_path="{token}/command/{command}/status".format(token=token,
                                                            command=HEALTH_CHECK_COMMAND_TYPE),
)
logger.debug("Composed command log topic: {}".format(topic_command_health_check))

topic_command_result_log = "kp1/{application_version}/{service_instance}/{resource_path}".format(
    application_version=appVersionName,
    service_instance=CEX_INSTANCE_NAME,
    resource_path="{token}/result/{command}".format(token=token, command=HEALTH_CHECK_COMMAND_TYPE),
)
logger.debug("Composed command result log topic: {}".format(topic_command_result_log))


def log_command_handler(client, userdata, message):
    """
    Handles HEALTH_CHECK command
    """
    logger.info("Received HEALTH_CHECK command: topic [{}]\nbody [{}]".format(message.topic, str(
        message.payload.decode("utf-8"))))
    command_result = compose_command_result_payload(message)
    client.publish(topic=topic_command_result_log, payload=command_result)
    logger.info("Published HEALTH_CHECK command result: topic [{}]\nbody [{}]".format(
        topic_command_result_log, str(command_result.decode("utf-8"))))
    log_data_sample_payload = compose_log_data_sample()
    client.publish(topic=topic_data_collection, payload=log_data_sample_payload)


def compose_command_result_payload(message):
    command_payload = json.loads(str(message.payload.decode("utf-8")))
    command_result_list = []
    for command in command_payload:
        command_result = {"id": command['id'], "statusCode": 200, "payload": "done"}
        command_result_list.append(command_result)
    return json.dumps(
        command_result_list
    )


def on_message(client, userdata, message):
    """
    Logs MQTT messages received from the server.
    """
    logger.info("Message received: topic [{}]\nbody [{}]".format(message.topic, str(
        message.payload.decode("utf-8"))))


# Initiate server connection
client = mqtt.Client(client_id=client_id)
client.on_message = on_message
client.message_callback_add(topic_command_health_check, log_command_handler)
connect_to_server(client=client, host=host, port=port)
# Start the loop
client.loop_start()

location_index = 0
battery_level = 100

# Send device metadata once on the first connection
metadata = compose_metadata()
client.publish(topic=topic_metadata, payload=metadata)
logger.info("Sent metadata: {0}\n".format(metadata))

dirname = os.path.dirname(__file__)
filename = os.path.join(dirname, "location.json")
location_data = load_json(filename)

# Send device data sample in loop
while 1:
    if location_index == len(location_data) - 1:
        location_index = 0
    if battery_level < 1:
        battery_level = 100

    payload = compose_data_sample(location_data[location_index], battery_level)
    result = client.publish(topic=topic_data_collection, payload=payload)

    if result.rc != 0:
        logger.info("Server connection lost, attempting to reconnect")
        connect_to_server(client=client, host=host, port=port)
    else:
        logger.debug("{0}: Sent next data sample: {1}".format(token, payload))

    time.sleep(5)
    location_index = location_index + 1
    battery_level = battery_level - 0.3
