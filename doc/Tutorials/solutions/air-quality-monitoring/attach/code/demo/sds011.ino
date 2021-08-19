#include "SdsDustSensor.h"

#define RX_PIN 0
#define TX_PIN 2
SdsDustSensor sds(RX_PIN, TX_PIN);

void setup() {
  Serial.begin(115200);
  sds.begin();

  Serial.println(sds.queryFirmwareVersion().toString()); // prints firmware version
  Serial.println(sds.setActiveReportingMode().toString()); // ensures sensor is in 'active' reporting mode
  Serial.println(sds.setContinuousWorkingPeriod().toString()); // ensures sensor has continuous working period - default but not recommended
}

void loop() {
  PmResult pm = sds.readPm();
  if (pm.isOk()) {
    Serial.printf("PM2.5 = %.2f; PM10 = %.2f\n", pm.pm25, pm.pm10);
  } else {
    Serial.print("Could not read values from sensor, reason: ");
    Serial.println(pm.statusToString());
  }

  delay(5000);
}
