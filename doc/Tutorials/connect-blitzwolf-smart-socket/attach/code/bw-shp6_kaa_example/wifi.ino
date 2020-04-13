#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>
#include <WiFiUdp.h>

#define SerialMon Serial

//#define DEBUG_UDP

#ifdef DEBUG_UDP
  char remoteServer[] = "0.0.0.0"; //remote server ip for debugging
  unsigned int remotePort = 0; //remote server port for debugging
  WiFiUDP Udp;
#endif

const char* ssid = WIFI_SSID;
const char* password = WIFI_PASS;

void setupWifi() {
  delay(10);
  SerialMon.println();
  SerialMon.print("Connecting to ");
  SerialMon.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  String ipstring = (
               String(WiFi.localIP()[0]) + "." +
               String(WiFi.localIP()[1]) + "." +
               String(WiFi.localIP()[2]) + "." +
               String(WiFi.localIP()[3])
             );
  PRINT_DBG("WiFi connected\n");
  PRINT_DBG("IP address: %s\n", ipstring.c_str());
}

void setupOta() {
  String otaHostname = "ESP8266-";
  otaHostname += String(WiFi.macAddress());
  PRINT_DBG("OTA hostname: %s\n", otaHostname.c_str());
  ArduinoOTA.setHostname(otaHostname.c_str());
  ArduinoOTA.begin();
}

void printMsg(const char * msg, ...) {
  char buff[256];
  va_list args;
  va_start(args, msg);
  vsnprintf(buff, sizeof(buff) - 2, msg, args);
  buff[sizeof(buff) - 1] = '\0';
  SerialMon.print(buff);
#ifdef DEBUG_UDP
  Udp.beginPacket(remoteServer, remotePort);
  Udp.write(buff, strlen(buff));
  Udp.endPacket();
#endif
}

void writeMsg(const char * msg, unsigned int len) {
  SerialMon.write(msg, len);
  SerialMon.println();
#ifdef DEBUG_UDP
  Udp.beginPacket(remoteServer, remotePort);
  Udp.write(msg, len);
  Udp.write("\n");
  Udp.endPacket();
#endif
}
