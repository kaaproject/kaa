#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define RECONNECT_TIME                  5000 //ms
#define SEND_TIME                       3000 //ms

char mqtt_host[] = KAA_HOST;
unsigned int mqtt_port = KAA_PORT;

unsigned long now = 0;
unsigned long last_reconnect = 0;
unsigned long last_msg = 0;

void callback(char* topic, byte* payload, unsigned int length) {
  PRINT_DBG("Message arrived [%s] ", topic);
  WRITE_DBG((char*)payload, length);
  messageArrivedCallbackKaa(topic, (char*)payload, length);
}

void reconnect() {
  PRINT_DBG("Attempting MQTT connection to %s:%u ...", mqtt_host, mqtt_port);
  // Create client ID
  String clientId = "ESP8266Client-";
  clientId += String(ESP.getChipId());
  // Attempt to connect
  if (client.connect(clientId.c_str()))
  {
    PRINT_DBG("connected\n");
    PRINT_DBG("MQTT ClientID: %s\n", clientId.c_str());
    connectKaa();
  } else
  {
    PRINT_DBG("failed, rc=%d try again in %d milliseconds\n", client.state(), RECONNECT_TIME);
  }
}

void mqttLoop() {
  if (!client.connected())
  {
    //Check connection
    now = millis();
    if ( ((now - last_reconnect) > RECONNECT_TIME) || (now < last_reconnect) )
    {
      last_reconnect = now;
      reconnect();
    }
  } else {
    //Send
    now = millis();
    if ( ((now - last_msg) > SEND_TIME) || (now < last_msg) )
    {
      last_msg = now;
      sendShp();
    }
  }
  client.loop();
}

void setupMqtt() {
  client.setServer(mqtt_host, mqtt_port);
  client.setCallback(callback);
}
