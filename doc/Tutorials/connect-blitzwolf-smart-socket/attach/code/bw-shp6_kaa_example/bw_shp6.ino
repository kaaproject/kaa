#include "HLW8012.h"
#include <ArduinoJson.h>

//Choose your version
#define BW_SHP6_10A
//#define BW_SHP6_16A

#define BUTTON_PIN                      13
#define RELAY_LED_PIN                   0
#define LINK_LED_PIN                    2
#define RELAY_PIN                       15

#define SEL_PIN                         12
#define CF_PIN                          5

#if defined( BW_SHP6_10A ) 
# define CF1_PIN                         14
# define CURRENT_RESISTOR                0.007
# define VOLTAGE_RESISTOR_UPSTREAM       (5*200000)
# define VOLTAGE_RESISTOR_DOWNSTREAM     (4000)
#elif defined( BW_SHP6_16A )
# define CF1_PIN                         4
# define CURRENT_RESISTOR                0.005
# define VOLTAGE_RESISTOR_UPSTREAM       (5*200000)
# define VOLTAGE_RESISTOR_DOWNSTREAM     (3750)
#else
# error Define your socket version
#endif

#define CURRENT_MODE                    LOW

HLW8012 bl0937;

unsigned int active_pow = 0;
unsigned int voltage = 0;
double current = 0;
unsigned int apparent_power = 0;
double power_factor = 0;

void setupShp() {
  pinMode(RELAY_LED_PIN, OUTPUT);
  pinMode(LINK_LED_PIN, OUTPUT);
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, HIGH);
  digitalWrite(RELAY_LED_PIN, !HIGH);
  bl0937.begin(CF_PIN, CF1_PIN, SEL_PIN, CURRENT_MODE, false, 500000);
  bl0937.setResistors(CURRENT_RESISTOR, VOLTAGE_RESISTOR_UPSTREAM, VOLTAGE_RESISTOR_DOWNSTREAM);
}

void sendShp() {
    active_pow = bl0937.getActivePower();
    voltage = bl0937.getVoltage();
    current = bl0937.getCurrent();
    apparent_power = bl0937.getApparentPower();
    
    bl0937.toggleMode();

    StaticJsonDocument<255> doc_data;

    doc_data.createNestedObject();
    doc_data[0]["voltage"] = voltage;
    doc_data[0]["current"] = current / 100.00;
    doc_data[0]["apparent_power"] = (float)apparent_power / 1000;

    PRINT_DBG("%s\n", doc_data.as<String>().c_str());
    sendDataRaw(doc_data.as<String>().c_str());
}

void shpLoop() {
  if (!digitalRead(BUTTON_PIN)) {
    PRINT_DBG("Button pressed");
    digitalWrite(RELAY_PIN, !digitalRead(RELAY_PIN));
    digitalWrite(RELAY_LED_PIN, !digitalRead(RELAY_PIN));
    delay(500);
  }
}

void relayChangeState(bool state){
  digitalWrite(RELAY_PIN, state);
  digitalWrite(RELAY_LED_PIN, !digitalRead(RELAY_PIN));
}

int getRelayState(){
  return digitalRead(RELAY_PIN);
}
