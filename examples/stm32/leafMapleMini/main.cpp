#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <wirish/wirish.h>
#include <wirish/usb_serial.h>
#include <libmaple/systick.h>
#include <time.h>
#include <stdarg.h>
#include "libraries/kaa/kaa_error.h"
#include "libraries/kaa/kaa_context.h"
#include "libraries/kaa/utilities/kaa_log.h"
#include "libraries/kaa/platform/ext_system_logger.h"
#include "libraries/kaa/platform/ext_sha.h"
#include "libraries/kaa/platform/ext_status.h"
#include "libraries/kaa/platform/ext_configuration_persistence.h"
#include "libraries/kaa/platform/ext_key_utils.h"
#include "libraries/kaa/platform-impl/stm32/leafMapleMini/esp8266/esp8266_serial.h"
#include "libraries/kaa/platform-impl/stm32/leafMapleMini/esp8266/chip_specififc.h"
#include "libraries/kaa/platform/kaa_client.h"
#include "libraries/kaa/kaa_profile.h"
#include "libraries/kaa/platform/defaults.h"
#include "libraries/kaa/kaa_common_schema.h"
#include "libraries/kaa/kaa_logging.h"
#include "libraries/kaa/kaa_configuration_manager.h"
#include "libraries/kaa/platform/mem.h"
#include "libraries/kaa/gen/kaa_geo_fencing_event_class_family.h"
#include "libraries/kaa/kaa_user.h"

#include "kaa_public_key.h"

#define NSS 7
#define CH_PD 22
#define ESP8266_RST 20

#define RX_BUFFER_SIZE 256 //1460

#define TRACE_DELAY 100

#define ESP_SERIAL Serial1

#define RFID_READER         Serial2
#define RFID_BAUD_RATE      9600
#define RFID_LENGTH         8
#define RFID_READ_DELAY     100 // Delay before reading RFID (in msec)

#define RFID_LEFT_GUARD     2
#define RFID_RIGHT_GUARD    3
#define RFID_SKIP_LEFT      2
#define RFID_SKIP_RIGHT     2
#define RFID_MAX_NUMBER     0xFFFFFFFF

#define SSID "Econais"
#define PWD "Cha5hk123"

#define KAA_USER_ID            "kaa"
#define KAA_USER_ACCESS_TOKEN  "token"

typedef int64_t rfid_t;



static esp8266_serial_t *esp8266_serial;
static kaa_client_t *kaa_client;

#define UNKNOWN_GEOFENCING_ZONE_ID    -1
static int current_zone_id = UNKNOWN_GEOFENCING_ZONE_ID;

#define DBG_SIZE 256
static char dbg_array[DBG_SIZE];

static rfid_t last_detected_rfid = RFID_MAX_NUMBER;



void esp8266_serial_init(esp8266_serial_t **serial, HardwareSerial *hw_serial, uint32_t baud_rate);



void setup()
{
    pinMode(CH_PD, OUTPUT);
    pinMode(ESP8266_RST, OUTPUT);
    pinMode(BOARD_BUTTON_PIN, INPUT);
    pinMode(BOARD_LED_PIN, OUTPUT);

    systick_enable();

    esp8266_serial_init(&esp8266_serial, &Serial3, 115200);
    if (!esp8266_serial) {
        debug("Serial Initialization failed, no memory\r\n");
        return;
    }

    RFID_READER.begin(RFID_BAUD_RATE);

    kaa_client_props_t props;
    props.serial = esp8266_serial;
    props.wifi_ssid = SSID;
    props.wifi_pswd = PWD;
    bool need_deallocation;
    ext_get_endpoint_public_key(&props.kaa_public_key, &props.kaa_public_key_length, &need_deallocation);
    kaa_error_t error = kaa_client_create(&kaa_client, &props);
    if (error) {
        debug("Failed to init Kaa client, error code %d\r\n", error);
        return;
    }

    kaa_profile_profile_t *profile = kaa_profile_profile_create();
    profile->platform = kaa_string_copy_create("LeafMapleMini");
    error = kaa_profile_manager_update_profile(
            kaa_client_get_context(kaa_client)->profile_manager
            , profile);
    if (error) {
        debug("Failed to update Kaa profile, error code %d\r\n", error);
        return;
    }

    error = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                  , KAA_USER_ID
                                                  , KAA_USER_ACCESS_TOKEN);
    if (error) {
        debug("Failed to attach to user '%s', error code %d\r\n", KAA_USER_ID, error);
        return;
    }
}

void sendRFIDLog(rfid_t rfid)
{
    kaa_context_t *kaa_context = kaa_client_get_context(kaa_client);

    kaa_user_log_record_t *log_record = kaa_logging_rfid_log_create();
    if (log_record) {
        log_record->tag = rfid;

        kaa_error_t error = kaa_logging_add_record(kaa_context->log_collector, log_record);
        if (error) {
            debug("Failed to add log record, code %d\r\n", error);
        } else {
            debug("Log record sent");
        }
        log_record->destroy(log_record);
    } else {
        debug("Failed to allocate log record\r\n");
    }
}

void notifyOfNewFencingZone(int zone_id)
{
    if (zone_id != UNKNOWN_GEOFENCING_ZONE_ID && current_zone_id != zone_id) {
        current_zone_id = zone_id;
        kaa_geo_fencing_event_class_family_geo_fencing_position_t position;

        switch (zone_id) {
        case ENUM_GEO_FENCING_ZONE_ID_HOME:
            position = ENUM_GEO_FENCING_POSITION_HOME;
            break;
        case ENUM_GEO_FENCING_ZONE_ID_NEAR:
            position = ENUM_GEO_FENCING_POSITION_NEAR;
            break;
        case ENUM_GEO_FENCING_ZONE_ID_AWAY:
            position = ENUM_GEO_FENCING_POSITION_AWAY;
            break;
        default:
            debug("Unknown zone");
            return;
        }

        kaa_geo_fencing_event_class_family_geo_fencing_position_update_t *position_update =
                          kaa_geo_fencing_event_class_family_geo_fencing_position_update_create();
        if (position_update) {
            position_update->position = position;

            kaa_context_t *kaa_context = kaa_client_get_context(kaa_client);
            kaa_error_t error = kaa_event_manager_send_kaa_geo_fencing_event_class_family_geo_fencing_position_update(
                                                                        kaa_context->event_manager, position_update, NULL);
            if (error) {
                debug("Failed to send 'Position Update' event, code %d\r\n", error);
            }

            position_update->destroy(position_update);
        } else {
            debug("Failed to allocate position update event\r\n");
        }
    }
}

void checkFencingPosition(rfid_t rfid)
{
    kaa_context_t *kaa_context = kaa_client_get_context(kaa_client);
    const kaa_root_configuration_t *configuration = kaa_configuration_manager_get_configuration(kaa_context->configuration_manager);

    if (configuration) {
        int new_zone_id = UNKNOWN_GEOFENCING_ZONE_ID;

        kaa_list_t *zones_it = configuration->zones;
        while (zones_it && (new_zone_id != UNKNOWN_GEOFENCING_ZONE_ID)) {
            kaa_configuration_geo_fencing_zone_t *zone = (kaa_configuration_geo_fencing_zone_t *)kaa_list_get_data(zones_it);
            if (current_zone_id != zone->id) {
                kaa_list_t *zone_tag_it = zone->tags;
                while (zone_tag_it && (new_zone_id != UNKNOWN_GEOFENCING_ZONE_ID)) {
                    int64_t *tag = (int64_t *)kaa_list_get_data(zone_tag_it);
                    if (*tag == rfid) {
                        new_zone_id = zone->id;
                    }
                    zone_tag_it = kaa_list_next(zone_tag_it);
                }
            }
            zones_it = kaa_list_next(zones_it);
        }

        if (new_zone_id != UNKNOWN_GEOFENCING_ZONE_ID) {
            debug("New zone detected, id=%d", new_zone_id);
            notifyOfNewFencingZone(new_zone_id);
        }
    } else {
        debug("Skip check fencing position: configuration is null\r\n");
    }
}

#define SKIP_BYTES(device, bytes_number) \
                 {  \
                      size_t counter = (bytes_number); \
                      while (counter-- > 0 ) { device.read(); } \
                 }

void readRFID()
{
    if (RFID_READER.available() > 0) {
        char buffer[RFID_LENGTH + 1];
        buffer[RFID_LENGTH] = '\0';

        delay(RFID_READ_DELAY);

        if (RFID_READER.read() == RFID_LEFT_GUARD) {
            SKIP_BYTES(RFID_READER, RFID_SKIP_LEFT);

            for (int i = 0; i < RFID_LENGTH ; ++i) {
                buffer[i] = RFID_READER.read();
            }

            SKIP_BYTES(RFID_READER, RFID_SKIP_RIGHT);

            if (RFID_READER.read() == RFID_RIGHT_GUARD) {
                RFID_READER.flush();
                rfid_t rfid = strtoull(buffer, NULL, 16);

                SerialUSB.print("Scanned ");
                SerialUSB.println(rfid);

                if (rfid != last_detected_rfid) {
                    last_detected_rfid = rfid;

                    SerialUSB.print("Detected new ");
                    SerialUSB.println(last_detected_rfid);

                    sendRFIDLog(rfid);
                    checkFencingPosition(rfid);
                }
            } else {
                RFID_READER.flush();
            }
        }
    }
}

void process(void *context)
{
    uint8 l = 0;
    if (SerialUSB.available()) {
        l = SerialUSB.read();
        if (l == 'S') {
            debug("\'S\' stop Kaa client\r\n");
            if (kaa_client)
                kaa_client_stop(kaa_client);
        }
    }

    readRFID();
}

void loop()
{
    if (kaa_client) {
        debug("Starting Kaa client, to stop press \'S\'\r\n");
        kaa_error_t error = kaa_client_start(kaa_client, process, (void*)kaa_client, 10);
        if (error) {
            debug("Error running Kaa client, code %d\r\n", error);
        }
        //kaa_client_destroy(kaa_client);
        kaa_client = NULL;
        debug("Switching to COM mode with ESP8266\r\n");
    }

    uint8 l = 0;
    if (esp8266_serial_available(esp8266_serial)) {
        SerialUSB.write(esp8266_serial_read(esp8266_serial));
    }
    if (SerialUSB.available()) {
        l = SerialUSB.read();
        if (l == '\n') {
            esp8266_serial_write(esp8266_serial, "\r");
        }
        esp8266_serial_write_byte(esp8266_serial, l);
        if (l == '\r') {
            esp8266_serial_write(esp8266_serial, "\n");;
        }
    }

//    readRFID();
}

// Force init to be called *first*, i.e. before static object allocation.
// Otherwise, statically allocated objects that need libmaple may fail.
__attribute__((constructor)) void premain() {
    init();
}

int main(void) {
    setup();

    while (true) {
        loop();
    }
    return 0;
}


/**
 * Kaa STM32 Leaf Maple mini ext_key_utils implementation
 */

void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = (char *)KAA_PUBLIC_KEY_DATA;
    *buffer_size = KAA_PUBLIC_KEY_LENGTH;
    *needs_deallocation = false;
}

/**
 * Kaa STM32 Leaf Maple mini ext_configuration_persistence implementation
 */

void ext_configuration_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = false;
}

void ext_configuration_store(const char *buffer, size_t buffer_size)
{
}

/**
 * Kaa STM32 Leaf Maple mini ext_status implementation
 */

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = false;
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
}

/*
 * Kaa STM32 specific logger implementation
 */

#define USB_DELAY 5
#define USB_TIMEOUT 100
#define CHECK_USB(usb_delay, usb_timeout) { time_t start = millis(); \
                                            while((SerialUSB.pending() > 0)) { \
                                                delay(usb_delay); \
                                                if ((millis() - start) > usb_timeout) \
                                                    break;\
                                            } \
                                          }


void ext_write_log(FILE * sink, const char * buffer, size_t message_size)
{
    if (!buffer) {
        return;
    }
    char *bf = (char *)buffer;
    bf[message_size-2] = 0;
    debug("%s\r\n", bf);
    return;
}


time_t ext_get_systime()
{
    return millis() / 1000;
}



/*
 * Chip specific implementation
 */
time_t get_sys_milis()
{
    return millis();
}

uint32_t get_sys_max(uint32_t s1, uint32_t s2)
{
    return max(s1,s2);
}

void ledOn()
{
    digitalWrite(BOARD_LED_PIN, HIGH);
}

void ledOff()
{
    digitalWrite(BOARD_LED_PIN, LOW);
}

void esp8266_reset()
{
    //Enable Chip Select signal;
    digitalWrite(CH_PD, LOW);
    delay(200);
    digitalWrite(CH_PD, HIGH);
    //reset chip
    digitalWrite(ESP8266_RST, LOW);
    delay(500);
    digitalWrite(ESP8266_RST, HIGH);
    delay(1000);
}

void debug(const char* format, ...)
{
    va_list args;
    va_start(args, format);

    int s = vsnprintf(dbg_array, DBG_SIZE, format, args);
    va_end(args);

    CHECK_USB(USB_DELAY, USB_TIMEOUT);
    SerialUSB.write(dbg_array, s);

}


/*
 * esp8266 serial specific implementation
 */

struct esp8266_serial_t {
    uint32_t baud_rate;
    HardwareSerial *hw_serial;
};

void esp8266_serial_init(esp8266_serial_t **serial, HardwareSerial *hw_serial, uint32_t baud_rate)
{
    if (!serial || !hw_serial)
        return;

    esp8266_serial_t *self = (esp8266_serial_t *)calloc(1,sizeof(esp8266_serial_t));
    if (!self) {
        *serial = NULL;
        return;
    }

    esp8266_reset();

    self->hw_serial = hw_serial;
    hw_serial->begin(baud_rate);
    *serial = self;
}

void esp8266_serial_end(esp8266_serial_t *serial) {
    if (!serial)
        return;
    serial->hw_serial->end();
}

uint8 esp8266_serial_read(esp8266_serial_t *serial)
{
    if (!serial)
        return 0;
    return serial->hw_serial->read();
}

bool esp8266_serial_available(esp8266_serial_t *serial)
{
    if (!serial)
        return false;
    return true ? serial->hw_serial->available() : false;
}

void esp8266_serial_write(esp8266_serial_t *serial, const char *message)
{
    if (!serial || !message)
        return;
    serial->hw_serial->write(message);
}

void esp8266_serial_write_byte(esp8266_serial_t *serial, const uint8 byte)
{
    if (!serial)
            return;
    serial->hw_serial->write(byte);
}

void esp8266_serial_write_buffer(esp8266_serial_t *serial, const void *buffer, const uint32 size)
{
    if (!serial)
                return;
    serial->hw_serial->write(buffer, size);
}

void esp8266_serial_write_command(esp8266_serial_t *serial, const char *command)
{
    if (!serial || !command)
        return;
    serial->hw_serial->println(command);
}
