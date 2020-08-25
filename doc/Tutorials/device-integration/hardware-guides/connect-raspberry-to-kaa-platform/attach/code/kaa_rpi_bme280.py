import json
import random
import signal
import string
import time
import bme280

import paho.mqtt.client as mqtt

KPC_HOST = "mqtt.cloud.kaaiot.com"  # Kaa Cloud plain MQTT host
KPC_PORT = 1883  # Kaa Cloud plain MQTT port

ENDPOINT_TOKEN = ""       # Paste endpoint token
APPLICATION_VERSION = ""  # Paste application version


class KaaClient:

    def __init__(self, client):
        self.client = client
        self.metadata_update_topic = f'kp1/{APPLICATION_VERSION}/epmx/{ENDPOINT_TOKEN}/update/keys'
        self.data_collection_topic = f'kp1/{APPLICATION_VERSION}/dcx/{ENDPOINT_TOKEN}/json'

    def connect_to_server(self):
        print(f'Connecting to Kaa server at {KPC_HOST}:{KPC_PORT} using application version {APPLICATION_VERSION} and endpoint token {ENDPOINT_TOKEN}')
        self.client.connect(KPC_HOST, KPC_PORT, 60)
        print('Successfully connected')

    def disconnect_from_server(self):
        print(f'Disconnecting from Kaa server at {KPC_HOST}:{KPC_PORT}...')
        self.client.loop_stop()
        self.client.disconnect()
        print('Successfully disconnected')

    def compose_metadata(self):
        return json.dumps([
            {
                "model": "BME/BMP 280",
                "fwVersion": "v0.0.1",
                "customer": "Andrew",
                "latitude": 40.71427,
                "longitude": -74.00597,
            }
        ])

    def compose_data_sample(self):
        # BME/BMP 280
        temperature, pressure, humidity = bme280.readBME280All()

        return json.dumps([
            {
                "timestamp": int(round(time.time() * 1000)),
                "temperature": round(temperature,1),
                "humidity": int(humidity),
                "pressure": round(pressure,2)
            }
        ])


def on_message(client, userdata, message):
    print(f'<-- Received message on topic "{message.topic}":\n{str(message.payload.decode("utf-8"))}')


def main():
    # Initiate server connection
    client = mqtt.Client(client_id=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6)))

    kaa_client = KaaClient(client)
    kaa_client.connect_to_server()
    client.on_message = on_message

    # Start the loop
    client.loop_start()

    # Send data samples in loop
    listener = SignalListener()

    payload = kaa_client.compose_metadata()
    result = kaa_client.client.publish(topic=kaa_client.metadata_update_topic, payload=payload)
    print(f'--> Sent message on topic "{kaa_client.metadata_update_topic}":\n{payload}')
    
    while listener.keepRunning:
        payload = kaa_client.compose_data_sample()
        result = kaa_client.client.publish(topic=kaa_client.data_collection_topic, payload=payload)
        if result.rc != 0:
            print('Server connection lost, attempting to reconnect')
            kaa_client.connect_to_server()
        else:
            print(f'--> Sent message on topic "{kaa_client.data_collection_topic}":\n{payload}')
        time.sleep(3)

    kaa_client.disconnect_from_server()


class SignalListener:
    keepRunning = True

    def __init__(self):
        signal.signal(signal.SIGINT, self.stop)
        signal.signal(signal.SIGTERM, self.stop)

    def stop(self, signum, frame):
        print('Shutting down...')
        self.keepRunning = False


if __name__ == '__main__':
    main()
