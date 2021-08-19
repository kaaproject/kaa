#include "SoftwareSerial.h"

#define RX_PIN 13
#define TX_PIN 15

SoftwareSerial co2SensorSerial(RX_PIN, TX_PIN);
byte co2Response[] = {0, 0, 0, 0, 0, 0, 0};

void setup() {
  Serial.begin(115200);
  co2SensorSerial.begin(9600);
}

void loop() {
  int co2 = requestCO2();
  Serial.printf("CO2 %d ppm\n", co2);
  delay(2000);
}

int requestCO2() {
  static byte readCommand[] = {0xFE, 0x44, 0x00, 0x08, 0x02, 0x9F, 0x25};

  int readAttempt = 0;
  while (!co2SensorSerial.available()) {
    co2SensorSerial.write(readCommand, 7);
    readAttempt++;
    delay(1000);
    if (readAttempt >= 5) {
      Serial.println(F("Failed to request CO2. Skipping it..."));
      return 0;
    }
  }

  int timeout = 0;
  while (co2SensorSerial.available() < 7) {
    timeout++;
    if (timeout > 10) {
      while (co2SensorSerial.available()) {
        co2SensorSerial.read();
      }
      break;
    }
    delay(50);
  }

  for (int i = 0; i < 7; i++) {
    co2Response[i] = co2SensorSerial.read();
  }

  int high = co2Response[3];
  int low = co2Response[4];

  int val = high * 256 + low;
  return val * 1;
}
