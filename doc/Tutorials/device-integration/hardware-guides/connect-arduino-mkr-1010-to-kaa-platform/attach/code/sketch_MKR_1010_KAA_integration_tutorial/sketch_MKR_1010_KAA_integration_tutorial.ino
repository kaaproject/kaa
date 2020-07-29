#include <SPI.h>
#include <WiFiNINA.h>//this lib(WiFiNINA by Arduino) must be installed via Libarary manager
#include <WiFiClient.h>
#include <ArduinoMqttClient.h>//this lib(ArduinoMqttClient by Arduino) must be installed via Libarary manager
#include <Arduino_MKRENV.h>//this lib(MKRENV by Arduino) must be installed via Libarary manager
#include <ArduinoJson.h>//this lib(Arduino_json by Arduino) must be installed via Libarary manager

const char* ssid = "";        // WiFi name
const char* password = "";    // WiFi password
const char* mqtt_server = "mqtt.cloud.kaaiot.com";
const String TOKEN = "";        // Endpoint token - you get (or specify) it during device provisioning
const String APP_VERSION = "";  // Application version - you specify it during device provisioning

const unsigned long fiveSeconds = 1 * 5 * 1000UL;
static unsigned long lastPublish = 0 - fiveSeconds;

const int led = A0;  // the analog output. WARNING: do not use PVM pins, because it affects on Light sensor.
const unsigned long checkLightInterval =  30UL;

const int ledMinVal = 750;

static unsigned long lastCheckedLight = 0;
static float currentBrightness = 0;
static float userBrightness = -1;
static int ledBrightness = 0;

WiFiClient wifiClient;
MqttClient mqttClient(wifiClient);

String configurationUpdatesTopic = "kp1/" + APP_VERSION + "/cmx/" + TOKEN + "/config/json/status";
String configurationRequestTopic = "kp1/" + APP_VERSION + "/cmx/" + TOKEN + "/config/json/1";
String configurationResponceTopic = "kp1/" + APP_VERSION + "/cmx/" + TOKEN + "/config/json/1/status";
String metricsPublishTopic = "kp1/" + APP_VERSION + "/dcx/" + TOKEN + "/json";
  

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
}

void loop() {
  setup_wifi();
  if (!mqttClient.connected()) {
    reconnect();
  }

  mqttClient.poll();

  unsigned long now = millis();
  if (now - lastPublish >= fiveSeconds) {
    lastPublish += fiveSeconds;

    //Getting data from the sensors
    DynamicJsonDocument telemetry(128);
    telemetry.createNestedObject();
    telemetry[0]["temperature"] = ENV.readTemperature();
    telemetry[0]["humidity"]    = ENV.readHumidity();
    currentBrightness = ENV.readIlluminance();
    telemetry[0]["illuminance"] = currentBrightness;
    telemetry[0]["pressure"]    = ENV.readPressure();
    telemetry[0]["uva"]         = ENV.readUVA();
    telemetry[0]["uvb"]         = ENV.readUVB();
    telemetry[0]["uvIndex"]     = ENV.readUVIndex();

    //Sending data to the Cloud
    String payload = telemetry.as<String>();
    mqttClient.beginMessage(metricsPublishTopic);
    mqttClient.print(payload.c_str());
    mqttClient.endMessage();
    
    Serial.println("Published on topic: " + metricsPublishTopic);
    Serial.println("payload:  " + payload);
  }
  
  handleBrightness();    
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
  Serial.println("reconnect");
  
  while (!mqttClient.connected()) {
    Serial.println("Attempting MQTT connection...");
    
    char *client_id = "client-id-123ab";// some unique client id
    mqttClient.setId(client_id);
    
    if (!mqttClient.connect(mqtt_server, 1883)) {
      Serial.print("MQTT connection failed! Error code = ");
      Serial.println(mqttClient.connectError());
      Serial.println(" try again in 5 seconds");
      
      // Wait 5 seconds before retrying
      delay(5000);
    } else {
      Serial.println("You're connected to the MQTT broker!");
      Serial.println();
    
      // set the message receive callback
      mqttClient.onMessage(onMqttMessage);
     
      subscribeToConfiguration();  
    }
  }
}

void subscribeToConfiguration() {
  //Subscribe on configuration topic for receiving configuration updates 
  mqttClient.subscribe(configurationUpdatesTopic);
  Serial.println("Subscribed on topic: " + configurationUpdatesTopic);

  //Subscribe on configuration topic response
  mqttClient.subscribe(configurationResponceTopic);
  Serial.println("Subscribed on topic: " + configurationResponceTopic);

  // Sending configuration rquest to the server
  DynamicJsonDocument configRequest(128);
  configRequest["observe"] = true;
  String payload = configRequest.as<String>();
  Serial.println("Request configuration topic: " + configurationRequestTopic);
  Serial.print("Request configuration payload =  ");Serial.println(payload);   
  configRequest.clear();
  mqttClient.beginMessage(configurationRequestTopic);
  mqttClient.print(payload.c_str());
  mqttClient.endMessage();
 
}

void handleBrightness() {

  if(userBrightness < 0){
    analogWrite(led, 0);
    return;
  }
  
  unsigned long now = millis();
  if (now - lastCheckedLight >= checkLightInterval) {
    lastCheckedLight = now;
    
    DynamicJsonDocument telemetry(128);
    telemetry.createNestedObject();
    currentBrightness = ENV.readIlluminance();
    telemetry.clear();

    if(currentBrightness < userBrightness){
      if(ledBrightness < ledMinVal){
        ledBrightness = ledMinVal +  1;
      } else{
        ledBrightness += 1;
      }
      if(ledBrightness > 1023) ledBrightness = 1023;
    } else if(currentBrightness > userBrightness){
      ledBrightness-= 1;
      if(ledBrightness < ledMinVal) ledBrightness = 0;
    } 
    
    analogWriteResolution(10);
    analogWrite(led, ledBrightness);
  }
}

void onMqttMessage(int messageSize) {
  // we received a message, print out the topic and contents
  String topic = mqttClient.messageTopic();
  Serial.println("Received a message on topic '");
  Serial.print(topic);
  Serial.print("', length ");
  Serial.print(messageSize);

  //Getting payload
  byte buffer[messageSize];
  mqttClient.read(buffer, messageSize);
  Serial.print("  Payload: ");
  for(int i = 0 ; i < messageSize ; i++){
    Serial.print((char)buffer[i]);
  }
  Serial.println();

  if(topic == configurationResponceTopic || topic == configurationUpdatesTopic){
    Serial.println("Configuration received");

    //Getting brightness parameter from payload
    DynamicJsonDocument newConfig(1023);
    deserializeJson(newConfig, buffer);
    int brightness = newConfig.as<JsonVariant>()["config"].as<JsonVariant>()["brightness"].as<int>();
    Serial.print(" New brightness: ");Serial.println(String(brightness));
    
    if(brightness < 1){
      userBrightness = -1; 
      ledBrightness = 0; 
    } else {
      userBrightness = brightness;
    }
  }
}
