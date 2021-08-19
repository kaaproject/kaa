import json
import logging
import time
import signal
import sys

from air_station import AirStation
from kaa_client import KaaClient

# Change application version
application_version = 'c40t2bbefgnu0bit0esg-v1'
# If you revoked the token, update this value with new one
token = 'air-station-1'

mqtt_host = 'mqtt.cloud.kaaiot.com'
mqtt_port = 1883

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(message)s')

kaa_client = KaaClient(mqtt_host, mqtt_port, application_version, token)
air_station = AirStation(kaa_client)

def killhandle(signum, frame):
    logging.info("SIGTERM detected, shutting down")
    kaa_client.disconnect()
    sys.exit(0)

signal.signal(signal.SIGINT, killhandle)
signal.signal(signal.SIGTERM, killhandle)

logging.info(
    f"""
        Connect to Kaa using:
        Host: [{mqtt_host}]
        Port: [{mqtt_port}]
        Application version name: [{application_version}]
        Token: [{token}]
    """
)

# Start mqtt connection
kaa_client.connect()

metadata = air_station.get_device_metadata()
logging.info(f"Sending metadata [{metadata}]")
kaa_client.publish_metadata(metadata)

class ReportingFrequency:

    def __init__(self):
        self.frequency_min = 1

    def get_frequency(self):
        return self.frequency_min

    def set_frequency(self, new_frequency_min):
        self.frequency_min = new_frequency_min

reporting_frequency = ReportingFrequency()

def handle_report_frequency_config(_, __, msg):
    config_response = json.loads(msg.payload)
    logging.info(f"Handling configuration: {config_response}")

    if "statusCode" not in config_response or "config" not in config_response:
        logging.error(f"Malformed config response was received. Ignoring it: {config_response}")
        return

    if config_response["statusCode"] != 200:
        logging.error(f"Config object with {config_response['statusCode']} status code was received. Ignoring it: {config_response}")
        return

    if "reportingFrequency" not in config_response["config"] or type(config_response["config"]["reportingFrequency"]) is not int:
        logging.error(f"Config has missed or invalid 'reportingFrequency' key. Ignoring it: {config_response}")
        return

    reporting_frequency.set_frequency(config_response["config"]["reportingFrequencyMin"])


while True:
    data_sample = air_station.get_data_sample()
    logging.info(f"Sending data sample: {json.dumps(data_sample)}")
    kaa_client.publish_data_collection(data_sample)
    kaa_client.add_configuration_status_handler(handle_report_frequency_config)
    time.sleep(30)
