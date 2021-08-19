import random
import time

from kaa_client import KaaClient


class AirStation(object):
    def __init__(self, client: KaaClient):
        self.kaa_client = client
        self.time = time.time()

    @staticmethod
    def get_device_metadata():
        return {
            "microchip": "ESP8266",
            "model": "Node MCU",
            "ip": "241.186.133.98",
            "mac": "5c:cf:7f:06:28:53",
            "serial": "403539"
        }

    @staticmethod
    def get_data_sample():
        return {
            "humidity": random.randint(30, 70),
            "temperature": random.randint(10, 30),
            "aqi": random.randint(20, 100),
            "co2": random.randint(600, 1100),
            "pm25": random.randint(20, 100),
            "pm10": random.randint(40, 110),
        }
