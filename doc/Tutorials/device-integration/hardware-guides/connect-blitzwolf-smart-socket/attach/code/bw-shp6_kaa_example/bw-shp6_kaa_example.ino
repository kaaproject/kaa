#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>
#include <WiFiUdp.h>
#include <PubSubClient.h>

#define PRINT_DBG(...) printMsg(__VA_ARGS__)
#define WRITE_DBG(...) writeMsg(__VA_ARGS__)

#define WIFI_SSID "**********"
#define WIFI_PASS "**********"

#define KAA_HOST "**********"
#define KAA_PORT 1883
#define KAA_TOKEN "**********"
#define KAA_APP_VERSION "**********"

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200);
  setupWifi();
  setupOta();
  setupMqtt();
  setupShp();
}

void loop() {
  ArduinoOTA.handle();
  mqttLoop();
  shpLoop();
}
