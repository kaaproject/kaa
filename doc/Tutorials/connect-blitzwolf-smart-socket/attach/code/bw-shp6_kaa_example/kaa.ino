#include <PubSubClient.h>
#include <ArduinoJson.h>

#define OUTPUT_1_NAME "relay_1"

char kaa_token[11] = KAA_TOKEN;
char kaa_application_version[100] = KAA_APP_VERSION;

char protocol_name[] = "kp1";
char epmx_instance_name[] = "epmx";
char cmx_instance_name[] = "cmx";
char dcx_instance_name[] = "dcx";
char cex_instance_name[] = "cex";

char output_switch_command_type[] = "RELAY_SWITCH";

char metadata_update_topic[100];
char config_topic[100];
char data_topic[100];
char command_topic[100];
char command_result_topic[100];


void composeTopics() {
  sprintf(metadata_update_topic, "%s/%s/%s/%s/update/keys", protocol_name, kaa_application_version, epmx_instance_name, kaa_token);
  sprintf(config_topic, "%s/%s/%s/%s/config/json", protocol_name, kaa_application_version, cmx_instance_name, kaa_token);
  sprintf(data_topic, "%s/%s/%s/%s/json", protocol_name, kaa_application_version, dcx_instance_name, kaa_token);
  sprintf(command_topic, "%s/%s/%s/%s/command", protocol_name, kaa_application_version, cex_instance_name, kaa_token);
  sprintf(command_result_topic, "%s/%s/%s/%s/result", protocol_name, kaa_application_version, cex_instance_name, kaa_token);
}

void sendMetadata() {
  StaticJsonDocument<255> doc_data;
  char topic_buffer[200];
  sprintf( topic_buffer, "%s/%d", metadata_update_topic, random(1, 100000) );

  String ipstring = (
                      String(WiFi.localIP()[0]) + "." +
                      String(WiFi.localIP()[1]) + "." +
                      String(WiFi.localIP()[2]) + "." +
                      String(WiFi.localIP()[3])
                    );

  doc_data["name"] = "Smart socket";
#if defined( BW_SHP6_10A ) 
  doc_data["model"] = "BW-SHP6 10A";
#elif defined( BW_SHP6_16A )
  doc_data["model"] = "BW-SHP6 16A";
#else
#warning Define your socket version
#endif
  doc_data["ip"] = ipstring;
  doc_data["mac"] = String(WiFi.macAddress());
  doc_data["serial"] = String(ESP.getChipId());

  PRINT_DBG("Publishing: %s %s\n", topic_buffer, doc_data.as<String>().c_str());
  client.publish(topic_buffer, doc_data.as<String>().c_str());
}

void connectKaa() {
  composeTopics();
  sendMetadata();
  client.subscribe(config_topic);
  sendOutputsState();
}

void messageArrivedCallbackKaa(char* topic_recv, char* payload_recv, unsigned int len) {
  char topic[512];
  char payload[1023];
  strncpy(topic, topic_recv, sizeof(topic) - 1);
  topic[sizeof(topic) - 1] = '\0';
  if (len < 1023) {
    strncpy(payload, payload_recv, len);
    payload[len] = '\0';
  }
  else {
    PRINT_DBG("Error. payload len >= 1023\n");
    return;
  }
  char* rest = topic;
  char* token = strtok_r(rest, "/", &rest);
  if (!strcmp(token, protocol_name)) {
    token = strtok_r(rest, "/", &rest);
    if (!strcmp(token, kaa_application_version)) {
      token = strtok_r(rest, "/", &rest);
      //EPMX
      if (!strcmp(token, epmx_instance_name)) {
        PRINT_DBG("Message for EPMX\n");

      }
      //DCX
      else if (!strcmp(token, dcx_instance_name)) {
        PRINT_DBG("Message for DCX\n");

      }
      //CMX
      else if (!strcmp(token, cmx_instance_name)) {
        PRINT_DBG("Message for CMX\n");

      }
      //CEX
      else if (!strcmp(token, cex_instance_name)) {
        PRINT_DBG("Message for CEX\n");
        cexMessageHandler(rest, payload, len);
      }
    }
  }
}

void cexMessageHandler(char* topic_part, char* payload, unsigned int len) {
  char command_type[50];
  char* rest = topic_part;
  char* token = strtok_r(rest, "/", &rest);
  if (!strcmp(token, kaa_token)) {
    token = strtok_r(rest, "/", &rest);
    if (!strcmp(token, "command")) {
      token = strtok_r(rest, "/", &rest);
      strncpy(command_type, token, sizeof(command_type) - 1);
      command_type[sizeof(command_type) - 1] = '\0';
      token = strtok_r(rest, "/", &rest);
      if (!strcmp(token, "status")) {
        PRINT_DBG("\nCommand %s received\n", command_type);
        commandProcess(command_type, payload, len);
      }
      else if (!strcmp(token, "error")) {
        PRINT_DBG("\nCommand %s error\n", command_type);
        PRINT_DBG("%s\n", payload);
      }
    }
  }
}

void commandProcess(char* command_type, char* payload, unsigned int len) {
  if (!strcmp(command_type, output_switch_command_type)) {

    DynamicJsonDocument doc(1023);
    deserializeJson(doc, payload, len);
    JsonVariant json_var = doc.as<JsonVariant>();

    PRINT_DBG("Used command_id = %d\n", json_var[0]["id"].as<unsigned int>());
    int output_number = json_var[0]["payload"]["number"].as<int>();
    int output_state = json_var[0]["payload"]["state"].as<int>();
    changeOutputState(output_number, output_state);

    //Sending result to all command ids
    PRINT_DBG("number of ids = %d\n", json_var.size());
    DynamicJsonDocument doc_result(1023);
    for (int i = 0; i < json_var.size(); i++) {
      unsigned int command_id = json_var[i]["id"].as<unsigned int>();
      PRINT_DBG("command_id = %d\n", command_id);
      doc_result.createNestedObject();
      doc_result[i]["id"] = command_id;
      doc_result[i]["statusCode"] = 200;
      doc_result[i]["payload"] = "done";
    }
    PRINT_DBG("command_type = %s\n", command_type);
    sendCommandResultRaw(command_type, doc_result.as<String>().c_str());
  }
  else {
    PRINT_DBG("Unknown command\n");
  }
}

void changeOutputState(int output_number, int output_state) {
  relayChangeState(output_state);
  sendOutputsState();
}

void sendCommandResult(const char* command_type, int command_id) {
  char topic_buffer[200];
  char command_result_payload_buffer[200];

  sprintf( topic_buffer, "%s/%s", command_result_topic, command_type );
  sprintf( command_result_payload_buffer, "[{\"id\": %d, \"statusCode\": 200, \"payload\": \"done\"}]", command_id);
  PRINT_DBG("Publishing: %s %s\n", topic_buffer, command_result_payload_buffer);
  client.publish(topic_buffer, command_result_payload_buffer);
}

void sendCommandResultRaw(const char* command_type, const char* json_payload) {
  char topic_buffer[200];
  sprintf( topic_buffer, "%s/%s", command_result_topic, command_type );
  PRINT_DBG("Publishing: %s %s\n", topic_buffer, json_payload);
  client.publish(topic_buffer, json_payload);
}

void sendDataRaw(const char* payload) {
  char topic_buffer[200];
  sprintf( topic_buffer, "%s/%d", data_topic, random(1, 100000) );
  PRINT_DBG("Publishing: %s %s\n", topic_buffer, payload);
  client.publish(topic_buffer, payload);
}

void sendOutputsState() {
  StaticJsonDocument<255> doc_data;

  doc_data.createNestedObject();
  doc_data[0][OUTPUT_1_NAME] = getRelayState();
  
  sendDataRaw(doc_data.as<String>().c_str());
}

void requestConfig() {
  char topic_buffer[200];
  char request_config_payload[] = "{\"observe\": true}";
  sprintf( topic_buffer, "%s/%d", config_topic, random(1, 100000) );
  PRINT_DBG("Publishing: %s %s\n", topic_buffer, request_config_payload);
  client.subscribe("topic_buffer");
  client.publish(topic_buffer, request_config_payload);
}
