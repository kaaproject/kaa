# Simple MQTT-based command execution client for the Kaa IoT platform.
# Handles "reboot" and "zero" command types.
# See https://docs.kaaiot.io/KAA/docs/current/Tutorials/sending-commands-to-device/

import json
import paho.mqtt.client as mqtt
import random
import signal
import string
import time

KPC_HOST = "mqtt.cloud.kaaiot.com"  # Kaa Cloud plain MQTT host
KPC_PORT = 1883  # Kaa Cloud plain MQTT port

ENDPOINT_TOKEN = ""         # Paste endpoint token
APPLICATION_VERSION = ""    # Paste application version


class DataCollectionClient:

    def __init__(self, client):
        self.client = client
        self.data_collection_topic = f'kp1/{APPLICATION_VERSION}/dcx/{ENDPOINT_TOKEN}/json/32'

        command_reboot_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/command/reboot/status'
        self.client.message_callback_add(command_reboot_topic, self.handle_reboot_command)
        self.command_reboot_result_topik = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/result/reboot'

        command_zero_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/command/zero/status'
        self.client.message_callback_add(command_zero_topic, self.handle_zero_command)
        self.command_zero_result_topik = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/result/zero'

    def connect_to_server(self):
        print(f'Connecting to Kaa server at {KPC_HOST}:{KPC_PORT} using application version {APPLICATION_VERSION} and endpoint token {ENDPOINT_TOKEN}')
        self.client.connect(KPC_HOST, KPC_PORT, 60)
        print('Successfully connected')

    def disconnect_from_server(self):
        print(f'Disconnecting from Kaa server at {KPC_HOST}:{KPC_PORT}...')
        self.client.loop_stop()
        self.client.disconnect()
        print('Successfully disconnected')

    def handle_reboot_command(self, client, userdata, message):
        print(f'<--- Received "reboot" command on topic {message.topic} \nRebooting...')
        command_result = self.compose_command_result_payload(message)
        print(f'command result {command_result}')
        client.publish(topic=self.command_reboot_result_topik, payload=command_result)
        # With below approach we don't receive the command confirmation on the server side.
        # self.client.disconnect()
        # time.sleep(5)  # Simulate the reboot
        # self.connect_to_server()

    def handle_zero_command(self, client, userdata, message):
        print(f'<--- Received "zero" command on topic {message.topic} \nSending zero values...')
        command_result = self.compose_command_result_payload(message)
        client.publish(topic=self.data_collection_topic, payload=self.compose_data_sample(0, 0, 0))
        client.publish(topic=self.command_zero_result_topik, payload=command_result)

    def compose_command_result_payload(self, message):
        command_payload = json.loads(str(message.payload.decode("utf-8")))
        print(f'command payload: {command_payload}')
        command_result_list = []
        for command in command_payload:
            commandResult = {"id": command['id'], "statusCode": 200, "reasonPhrase": "OK", "payload": "Success"}
            command_result_list.append(commandResult)
        return json.dumps(
            command_result_list
        )

    def compose_data_sample(self, fuelLevel, minTemp, maxTemp):
        return json.dumps({
            'timestamp': int(round(time.time() * 1000)),
            'fuelLevel': fuelLevel,
            'temperature': random.randint(minTemp, maxTemp),
        })


def on_message(client, userdata, message):
    print(f'Message received: topic {message.topic}\nbody {str(message.payload.decode("utf-8"))}')


def main():
    # Initiate server connection
    client = mqtt.Client(client_id=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6)))

    data_collection_client = DataCollectionClient(client)
    data_collection_client.connect_to_server()

    client.on_message = on_message

    # Start the loop
    client.loop_start()

    fuelLevel, minTemp, maxTemp = 100, 95, 100

    # Send data samples in loop
    listener = SignalListener()
    while listener.keepRunning:

        payload = data_collection_client.compose_data_sample(fuelLevel, minTemp, maxTemp)

        result = data_collection_client.client.publish(topic=data_collection_client.data_collection_topic, payload=payload)
        if result.rc != 0:
            print('Server connection lost, attempting to reconnect')
            data_collection_client.connect_to_server()
        else:
            print(f'--> Sent message on topic "{data_collection_client.data_collection_topic}":\n{payload}')

        time.sleep(3)

        fuelLevel = fuelLevel - 0.3
        if fuelLevel < 1:
            fuelLevel = 100

    data_collection_client.disconnect_from_server()


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
