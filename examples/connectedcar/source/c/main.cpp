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
#include "libraries/kaa/gen/kaa_geo_fencing_event_class_family_definitions.h"
#include "libraries/kaa/kaa_user.h"

#include "kaa_public_key.h"

#define NSS 7
#define CH_PD 22
#define LEFT_LIGHT 21
#define RIGHT_LIGHT 26
#define ESP8266_RST 20

#define RX_BUFFER_SIZE 256 //1460

#define ESP_SERIAL &Serial3
#define ESP_SERIAL_BAUD 9600

#define LIGHT_BLINK_TIME 50
#define LIGHT_BLINK_NUMBER 6 //Should be even number, 6 mean 3 blink, with interval on/off LIGHT_BLIM_TIME


#define RFID_READER         Serial2
#define RFID_BAUD_RATE      9600
#define RFID_LENGTH         8
#define RFID_READ_DELAY     100 // Delay before reading RFID (in msec)

#define RFID_LEFT_GUARD     2
#define RFID_RIGHT_GUARD    3
#define RFID_SKIP_LEFT      2
#define RFID_SKIP_RIGHT     2
#define RFID_MAX_NUMBER     0xFFFFFFFF

#define SSID "KaaIoT"
#define PWD "cybervision2015"

#define KAA_USER_ID            "kaa"
#define KAA_USER_ACCESS_TOKEN  "token"

typedef int64_t rfid_t;



static esp8266_serial_t *esp8266_serial;
static kaa_client_t *kaa_client;

#define UNKNOWN_GEOFENCING_ZONE_ID    0xFFFF
static int current_zone_id = UNKNOWN_GEOFENCING_ZONE_ID;

#define DBG_SIZE 256
static char dbg_array[DBG_SIZE];

typedef enum {
    RFID_READ_WAIT_START = 0,
    RFID_READ_LEFT_GUARD,
    RFID_READ,
    RFID_READ_RIGHT_GUARD,
    RFID_READ_WAIT_FINISH
} rfid_read_state_t;

static rfid_t last_detected_rfid = RFID_MAX_NUMBER;
static char rfid_buffer[RFID_LENGTH + 1];
static int rfid_counter;
static rfid_read_state_t rfid_state;
static char rfid_char;



/* Light blink variables */
static uint32_t light_blink_time;
static int light_blink_counter;




void esp8266_serial_init(esp8266_serial_t **serial, HardwareSerial *hw_serial, uint32_t baud_rate);



void setup()
{
    pinMode(CH_PD, OUTPUT);
    pinMode(ESP8266_RST, OUTPUT);
    pinMode(BOARD_BUTTON_PIN, INPUT);
    pinMode(BOARD_LED_PIN, OUTPUT);
    pinMode(LEFT_LIGHT, OUTPUT);
    pinMode(RIGHT_LIGHT, OUTPUT);

    light_blink_counter = -1;
    systick_enable();

    esp8266_serial_init(&esp8266_serial, ESP_SERIAL, ESP_SERIAL_BAUD);
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
    light_blink_time = millis();
    lightOn(true, true);
    light_blink_counter = 0;
    kaa_context_t *kaa_context = kaa_client_get_context(kaa_client);

    kaa_user_log_record_t *log_record = kaa_logging_rfid_log_create();
    if (log_record) {
        log_record->tag = rfid;

        kaa_error_t error = kaa_logging_add_record(kaa_context->log_collector, log_record);
        if (error) {
            debug("Failed to add log record, code %d\r\n", error);
        } else {
            debug("Log record sent\r\n");
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

        debug("New zone detected, id=%d\r\n", current_zone_id);

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
        while (zones_it && (new_zone_id == UNKNOWN_GEOFENCING_ZONE_ID)) {
            kaa_configuration_geo_fencing_zone_t *zone = (kaa_configuration_geo_fencing_zone_t *)kaa_list_get_data(zones_it);
            kaa_list_t *zone_tag_it = zone->tags;

            while (zone_tag_it && (new_zone_id == UNKNOWN_GEOFENCING_ZONE_ID)) {
                int64_t *tag = (int64_t *)kaa_list_get_data(zone_tag_it);
                if (*tag == rfid) {
                    new_zone_id = zone->id;
                }

                zone_tag_it = kaa_list_next(zone_tag_it);
            }
            zones_it = kaa_list_next(zones_it);
        }

        if (new_zone_id == UNKNOWN_GEOFENCING_ZONE_ID) {
            new_zone_id = ENUM_GEO_FENCING_ZONE_ID_AWAY;
        }

        notifyOfNewFencingZone(new_zone_id);
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
        rfid_char = RFID_READER.read();
        switch (rfid_state) {
            case RFID_READ_WAIT_START:
                if (rfid_char == RFID_LEFT_GUARD) {
                    rfid_counter = 0;
                    rfid_state = RFID_READ_LEFT_GUARD;
                }
                break;
            case RFID_READ_LEFT_GUARD:
                rfid_counter++;
                if (rfid_counter >= RFID_SKIP_LEFT) {
                    rfid_counter = 0;
                    rfid_state = RFID_READ;
                }
                break;
            case RFID_READ:
                rfid_buffer[rfid_counter++] = rfid_char;
                if (rfid_counter >= RFID_LENGTH) {
                    rfid_buffer[rfid_counter] = '\0';
                    rfid_counter = 0;
                    rfid_state = RFID_READ_RIGHT_GUARD;
                }
                break;
            case RFID_READ_RIGHT_GUARD:
                rfid_counter++;
                if (rfid_counter >= RFID_SKIP_RIGHT) {
                    rfid_counter = 0;
                    rfid_state = RFID_READ_WAIT_FINISH;
                }
                break;
            case RFID_READ_WAIT_FINISH:
                if (rfid_char == RFID_RIGHT_GUARD) {
                    rfid_t rfid = strtoull(rfid_buffer, NULL, 16);
                    if (rfid != last_detected_rfid) {
                        last_detected_rfid = rfid;

                        debug("Scanned: %llu\r\n", rfid);

                        sendRFIDLog(rfid);
                        checkFencingPosition(rfid);
                    }

                } else {
                    debug("Error scan RFID, wait RIGHT Guard, but got 0x%02X", rfid_char);
                }
                rfid_counter = 0;
                rfid_state = RFID_READ_WAIT_START;
                break;
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

    if (light_blink_counter >= 0) {
        if ((millis() - light_blink_time) >= LIGHT_BLINK_TIME) {
            light_blink_counter++;
            light_blink_time = millis();
            if ((light_blink_counter % 2) == 0) {
                lightOn(true, true);
            } else {
                lightOff(true, true);
            }
        }
        if (light_blink_counter > LIGHT_BLINK_NUMBER) {
            lightOff(true, true);
            light_blink_counter = -1;
        }
    }

    readRFID();
}

void loop()
{
    if (kaa_client) {
        debug("Starting Kaa client, to stop press \'S\'\r\n");
        kaa_error_t error = kaa_client_start(kaa_client, process, (void*)kaa_client, 5);
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
#define USB_TIMEOUT 20
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

void lightOn(bool left, bool right)
{
    if (left)
        digitalWrite(LEFT_LIGHT, HIGH);
    if (right)
        digitalWrite(RIGHT_LIGHT, HIGH);
}

void lightOff(bool left, bool right)
{
    if (left)
        digitalWrite(LEFT_LIGHT, LOW);
    if (right)
        digitalWrite(RIGHT_LIGHT, LOW);
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

    //CHECK_USB(USB_DELAY, USB_TIMEOUT);
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
