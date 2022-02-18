import itertools
import json
import queue
import random
import string
import sys
import time

import paho.mqtt.client as mqtt

KPC_HOST = "mqtt.cloud.kaaiot.com"  # Kaa Cloud plain MQTT host
KPC_PORT = 1883                     # Kaa Cloud plain MQTT port

CURRENT_SOFTWARE_VERSION = ""   # Specify software that device currently uses (e.g., 0.0.1)

APPLICATION_VERSION = ""     # Paste your application version
ENDPOINT_TOKEN = ""          # Paste your endpoint token


class SoftwareClient:

    def __init__(self, client):
        self.client = client
        self.software_by_request_id = {}
        self.global_request_id = itertools.count()
        get_software_topic = f'kp1/{APPLICATION_VERSION}/cmx_ota/{ENDPOINT_TOKEN}/config/json/#'
        self.client.message_callback_add(get_software_topic, self.handle_software)

    def handle_software(self, client, userdata, message):
        if message.topic.split('/')[-1] == 'status':
            topic_part = message.topic.split('/')[-2]
            if topic_part.isnumeric():
                request_id = int(topic_part)
                print(f'<--- Received software response on topic {message.topic}')
                software_queue = self.software_by_request_id[request_id]
                software_queue.put_nowait(message.payload)
            else:
                print(f'<--- Received software push on topic {message.topic}:\n{str(message.payload.decode("utf-8"))}')
        else:
            print(f'<--- Received bad software response on topic {message.topic}:\n{str(message.payload.decode("utf-8"))}')

    def get_software(self):
        request_id = next(self.global_request_id)
        get_software_topic = f'kp1/{APPLICATION_VERSION}/cmx_ota/{ENDPOINT_TOKEN}/config/json/{request_id}'

        software_queue = queue.Queue()
        self.software_by_request_id[request_id] = software_queue

        print(f'---> Requesting software by topic {get_software_topic}')
        payload = {
            "configId": CURRENT_SOFTWARE_VERSION
        }
        self.client.publish(topic=get_software_topic, payload=json.dumps(payload))

        try:
            software = software_queue.get(True, 5)
            del self.software_by_request_id[request_id]
            return str(software.decode("utf-8"))
        except queue.Empty:
            print('Timed out waiting for software response from server')
            sys.exit()

def main():
    # Initiate server connection
    print(f'Connecting to Kaa server at {KPC_HOST}:{KPC_PORT} using application version {APPLICATION_VERSION} and endpoint token {ENDPOINT_TOKEN}')

    client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
    client = mqtt.Client(client_id=client_id)
    client.connect(KPC_HOST, KPC_PORT, 60)
    client.loop_start()

    software_client = SoftwareClient(client)

    # Fetch available software
    retrieved_software = software_client.get_software()
    print(f'Retrieved software from server: {retrieved_software}')

    time.sleep(5)
    client.disconnect()


if __name__ == '__main__':
    main()
