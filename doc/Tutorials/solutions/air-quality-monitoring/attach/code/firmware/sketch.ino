#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "kaa.h"
#include "bsec.h"
#include "SdsDustSensor.h"

#define KAA_SERVER "mqtt.cloud.kaaiot.com"
#define KAA_PORT 1883
#define KAA_TOKEN "air-station-1"
#define KAA_APP_VERSION ""

#define RECONNECT_TIME  5000 // ms

#define RX_PIN 0
#define TX_PIN 2

const char* ssid = "";
const char* password = "";

char mqtt_host[] = KAA_SERVER;
unsigned int mqtt_port = KAA_PORT;

unsigned long now = 0;
unsigned long lastReconnect = 0;
unsigned long lastMsg = 0;

WiFiClient espClient;
PubSubClient client(espClient);
Kaa kaa(&client, KAA_TOKEN, KAA_APP_VERSION);

SdsDustSensor sds(RX_PIN, TX_PIN);

Bsec iaqSensor;
bool bsecInitialized = false;
bool bme680Initialized = false;

int reportingFrequencyMillis = 60 * 1000; // one minute

SoftwareSerial co2SensorSerial(13, 15);
byte co2Response[] = {0, 0, 0, 0, 0, 0, 0};

#define ERROR_LOG_LEVEL "Error"
#define WARN_LOG_LEVEL "Warn"

#define PRINT_DBG(...) printMsg(__VA_ARGS__)

void printMsg(const char * msg, ...) {
  char buff[256];
  va_list args;
  va_start(args, msg);
  vsnprintf(buff, sizeof(buff) - 2, msg, args);
  buff[sizeof(buff) - 1] = '\0';
  Serial.print(buff);
}

void setup() {
  Serial.begin(115200);
  while (!Serial) delay(10);

  // WiFi and Kaa connection setup
  setupWiFi();
  client.setServer(mqtt_host, mqtt_port);
  client.setCallback(callback);
  bool success = client.setBufferSize(1000);
  if (!success) {
    PRINT_DBG("Failed to set buffer size for MQTT client\n");
  }

  // SDS011 setup
  sds.begin();
  Serial.println(sds.queryFirmwareVersion().toString());
  Serial.println(sds.setActiveReportingMode().toString());
  Serial.println(sds.setContinuousWorkingPeriod().toString());

  // SenseAir S8 setup
  co2SensorSerial.begin(9600);

  // BME680 and BSEC library setup
  Wire.begin();

  iaqSensor.begin(BME680_I2C_ADDR_SECONDARY, Wire);
  checkIaqSensorStatus();

  bsec_virtual_sensor_t sensorList[10] = {
    BSEC_OUTPUT_RAW_TEMPERATURE,
    BSEC_OUTPUT_RAW_PRESSURE,
    BSEC_OUTPUT_RAW_HUMIDITY,
    BSEC_OUTPUT_RAW_GAS,
    BSEC_OUTPUT_IAQ,
    BSEC_OUTPUT_STATIC_IAQ,
    BSEC_OUTPUT_CO2_EQUIVALENT,
    BSEC_OUTPUT_BREATH_VOC_EQUIVALENT,
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_TEMPERATURE,
    BSEC_OUTPUT_SENSOR_HEAT_COMPENSATED_HUMIDITY,
  };

  iaqSensor.updateSubscription(sensorList, 10, BSEC_SAMPLE_RATE_LP);
  checkIaqSensorStatus();
}

void loop() {
  // Checking connection
  if (!client.connected()) {
    now = millis();
    if (((now - lastReconnect) > RECONNECT_TIME) || (now < lastReconnect)) {
      lastReconnect = now;
      reconnect();
    }
    return;
  }
  client.loop();

  // Sending telemetry
  now = millis();
  if (((now - lastMsg) > reportingFrequencyMillis) || (now < lastMsg)) {
    lastMsg = now;
    sendTelemetry();
  }
}

void checkIaqSensorStatus() {
  if (iaqSensor.status == BSEC_OK) {
    bsecInitialized = true;
  } else {
    if (iaqSensor.status < BSEC_OK) {
      String err = "BSEC error code: " + String(iaqSensor.status);
      PRINT_DBG("%s\n", err.c_str());
      sendLogToKaa(ERROR_LOG_LEVEL, err);
    } else {
      String warning = "BSEC warning code : " + String(iaqSensor.status);
      PRINT_DBG("%s\n", warning.c_str());
      sendLogToKaa(WARN_LOG_LEVEL, warning);
    }
  }

  if (iaqSensor.bme680Status == BME680_OK) {
    bme680Initialized = true;
  } else {
    if (iaqSensor.bme680Status < BME680_OK) {
      String err = "BME680 error code : " + String(iaqSensor.bme680Status);
      PRINT_DBG("%s\n", err.c_str());
      sendLogToKaa(ERROR_LOG_LEVEL, err);
    } else {
      String warning = "BME680 warning code : " + String(iaqSensor.bme680Status);
      PRINT_DBG("%s\n", warning.c_str());
      sendLogToKaa(WARN_LOG_LEVEL, warning);
    }
  }
}

void composeAndSendMetadata() {
  String ip = (
                String(WiFi.localIP()[0]) + "." +
                String(WiFi.localIP()[1]) + "." +
                String(WiFi.localIP()[2]) + "." +
                String(WiFi.localIP()[3])
              );

  StaticJsonDocument<255> metadata;
  metadata["ip"] = ip;
  metadata["mac"] = String(WiFi.macAddress());
  metadata["serial"] = String(ESP.getChipId());
  metadata["microchip"] = "ESP8266";
  metadata["model"] = "Node MCU";

  kaa.sendMetadata(metadata.as<String>().c_str());
}

void fillWithSDS011Data(StaticJsonDocument<255> &data) {
  PmResult pm = sds.readPm();
  if (pm.isOk()) {
    data[0]["pm25"] = pm.pm25;
    data[0]["pm10"] = pm.pm10;
  } else {
    String err = "Failed to read measurements from SDS011 sensor. Reason: " + pm.statusToString();
    PRINT_DBG("%s\n", err.c_str());
    sendLogToKaa(ERROR_LOG_LEVEL, err);
  }
}

void fillWithBME680Data(StaticJsonDocument<255> &data) {
  if (iaqSensor.run()) {
    data[0]["temperature"] = iaqSensor.temperature;
    data[0]["pressure"] = iaqSensor.pressure / 130; // converting Pa to mmHg
    data[0]["humidity"] = iaqSensor.humidity;
    data[0]["aqi"] = iaqSensor.staticIaq;
    data[0]["co2Equivalent"] = iaqSensor.co2Equivalent;
    data[0]["vocEquivalent"] = iaqSensor.breathVocEquivalent;
  } else {
    String err = "Failed to read measurements from BME680 sensor";
    PRINT_DBG("%s\n", err.c_str());
    sendLogToKaa(ERROR_LOG_LEVEL, err);
    checkIaqSensorStatus();
  }
}

void fillWithSenseAirS8Data(StaticJsonDocument<255> &data) {
  int co2 = requestCO2();
  if (co2 != -1) {
    data[0]["co2"] = co2;
  } else {
    String err = "Failed to read CO2 from SenseAir S8 sensor";
    PRINT_DBG("%s\n", err.c_str());
    sendLogToKaa(ERROR_LOG_LEVEL, err);
  }
}

void sendTelemetry() {
  StaticJsonDocument<255> data;
  data.createNestedObject();

  fillWithBME680Data(data);
  fillWithSDS011Data(data);
  fillWithSenseAirS8Data(data);

  kaa.sendDataRawUnreliably(data.as<String>().c_str());
}

int requestCO2() {
  static byte readCommand[] = {0xFE, 0x44, 0x00, 0x08, 0x02, 0x9F, 0x25};

  int readAttempt = 0;
  while (!co2SensorSerial.available()) {
    co2SensorSerial.write(readCommand, 7);
    readAttempt++;
    delay(1000);
    if (readAttempt >= 5) {
      PRINT_DBG("Failed to request CO2. Skipping it...");
      return -1;
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

void sendLogToKaa(String level, String log) {
  StaticJsonDocument<255> data;
  data.createNestedObject();

  String withLevel = level + ": " + log;
  data[0]["log"] = withLevel;
  kaa.sendDataRawUnreliably(data.as<String>().c_str());
}

void setupWiFi() {
  delay(10);
  PRINT_DBG("Connecting to %s\n", ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    PRINT_DBG(".");
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

void configResponseCallback(char* requestStatus, char* payload, unsigned int len) {
  if (!strcmp(requestStatus, "status")) {
    updateReportingFrequencyFromConfig(payload, len);
  } else {
    PRINT_DBG("Error config response was received: %s; %s\n", requestStatus, payload);
  }
}

void configPushCallback(char* payload, unsigned int len) {
  updateReportingFrequencyFromConfig(payload, len);
}

void updateReportingFrequencyFromConfig(char* payload, unsigned int len) {
  DynamicJsonDocument doc(192);
  deserializeJson(doc, payload, len);
  JsonVariant json_var = doc.as<JsonVariant>();

  if (json_var.containsKey("config") && json_var["config"].containsKey("reportingFrequencyMin")) {
    int reportingFrequencyMin = json_var["config"]["reportingFrequencyMin"].as<int>();
    reportingFrequencyMillis = reportingFrequencyMin * 60 * 1000;
    PRINT_DBG("New reporting frequency: %d millis\n", reportingFrequencyMillis);
  } else {
    PRINT_DBG("Config payload without 'reportingFrequencyMin' key was received. Ignoring it...\n");
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  PRINT_DBG("Message arrived [%s] ", topic);
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  kaa.messageArrivedCallback(topic, (char*)payload, length);
}

void reconnect() {
  PRINT_DBG("Attempting MQTT connection to %s:%u ...", mqtt_host, mqtt_port);
  String clientId = "AQ-tutorial-device-" + String(ESP.getChipId());
  // Attempt to connect
  if (client.connect(clientId.c_str())) {
    PRINT_DBG("Connected to WiFi\n");
    kaa.connect();
    kaa.setConfigResponseCallback(&configResponseCallback);
    kaa.setConfigPushCallback(&configPushCallback);
    kaa.requestConfig();
    composeAndSendMetadata();
  } else {
    PRINT_DBG("failed, rc=%d try again in %d milliseconds\n", client.state(), RECONNECT_TIME);
  }
}
