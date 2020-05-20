#include <SPI.h>
#include <WiFiNINA.h>//this lib(WiFiNINA by Arduino) must be installed via Libarary manager
#include <WiFiClient.h>
#include <PubSubClient.h>//this lib(PubSubClient by Nick O'Leary) must be installed via Libarary manager
#include <Arduino_MKRENV.h>//this lib(MKRENV by Arduino) must be installed via Libarary manager
#include <ArduinoJson.h>//this lib(Arduino_json by Arduino) must be installed via Libarary manager

const char* ssid = "";        // WiFi name
const char* password = "";    // WiFi password
const char* mqtt_server = "mqtt.cloud.kaaiot.com";
const String TOKEN = "";        // Endpoint token - you get (or specify) it during device provisioning
const String APP_VERSION = "";  // Application version - you specify it during device provisioning


const unsigned long fiveSeconds = 1 * 5 * 1000UL;
static unsigned long lastPublish = 0 - fiveSeconds;

WiFiClient wifiClient;
PubSubClient client(wifiClient);

void setup() {
  
  Serial.begin(115200);

  //Check is MKV_ENV kit present:
  if (!ENV.begin()) {
    Serial.println("Failed to initialize MKR ENV shield!");
    while (1);
  }
 
  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("Please upgrade the firmware");
  }
  
  client.setServer(mqtt_server, 1883);
}

void loop() {
  setup_wifi();
  if (!client.connected()) {
    reconnect();
  }

  
  client.loop();

  unsigned long now = millis();
  if (now - lastPublish >= fiveSeconds) {
    lastPublish += fiveSeconds;

    String topicStr = "kp1/" + APP_VERSION + "/dcx/" + TOKEN + "/json";
    const char* topic = topicStr.c_str();
    boolean result = true;
    Serial.println("Published on topic: " + topicStr);

    //NOTE: "PubSubClient" uses default packet size (128 bytes) 
    //so, topic + paylode lenght should be less than 128 bytes, if size is bigger, then need to split data on few calls.
    //If you want to encrease packet size, look on MQTT_MAX_PACKET_SIZE define in PubSubClient library.
    DynamicJsonDocument telemetry(128);
    telemetry.createNestedObject();

    telemetry[0]["temperature"] = ENV.readTemperature();
    telemetry[0]["humidity"]    = ENV.readHumidity();
    String payload = telemetry.as<String>();
    result = result & client.publish_P(topic, payload.c_str(), false);
    
    telemetry.clear();
    telemetry[0]["pressure"]    = ENV.readPressure();
    telemetry[0]["illuminance"] = ENV.readIlluminance();
    payload = telemetry.as<String>();
    result = result & client.publish_P(topic, payload.c_str(), false);
    
    telemetry.clear();    
    telemetry[0]["uva"]         = ENV.readUVA();
    telemetry[0]["uvb"]         = ENV.readUVB();
    telemetry[0]["uvIndex"]     = ENV.readUVIndex();
    payload = telemetry.as<String>();
    result = result & client.publish_P(topic, payload.c_str(), false);
    Serial.print("publish result: ");Serial.println(result);
  }
}

void setup_wifi() {
  if (WiFi.status() != WL_CONNECTED) {
    delay(200);
    Serial.println();
    Serial.print("Connecting to [");Serial.print(ssid);Serial.println("]");
    WiFi.begin(ssid, password); 
    connectWiFi();
  }
}

void connectWiFi() {
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void reconnect() {
  while (!client.connected()) {
    Serial.println("Attempting MQTT connection...");
    char *client_id = "client-id-123ab";
    if (client.connect(client_id)) {
      Serial.println("Connected to WiFi");
      // ... and resubscribe
      subscribeToCommand();
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void subscribeToCommand() {
  String topic = "kp1/" + APP_VERSION + "/cex/" + TOKEN + "/command/SWITCH/status";
  client.subscribe(topic.c_str());
  Serial.println("Subscribed on topic: " + topic);
}
