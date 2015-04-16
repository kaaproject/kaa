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

#define NSS 7
#define CH_PD 22
#define ESP8266_RST 20

#define RX_BUFFER_SIZE 512 //1460

#define TRACE_DELAY 100

#define ESP_SERIAL Serial1

#define SSID "Econais"
#define PWD "Cha5hk123"

void esp8266_serial_init(esp8266_serial_t **serial, HardwareSerial *hw_serial, uint32_t baud_rate);


static esp8266_serial_t *esp8266_serial;

static kaa_client_t *kaa_client;


#define DBG_SIZE 512
static char dbg_array[DBG_SIZE];



void setup() {

    pinMode(CH_PD, OUTPUT);
    pinMode(ESP8266_RST, OUTPUT);
    pinMode(BOARD_BUTTON_PIN, INPUT);
    pinMode(BOARD_LED_PIN, OUTPUT);

    systick_enable();

    esp8266_serial_init(&esp8266_serial, &Serial3, 38400);
    if (!esp8266_serial) {
        debug("Serial Initialization failed, no memory\r\n");
        return;
    }

    kaa_client_props_t props;
    props.serial = esp8266_serial;
    props.wifi_ssid = SSID;
    props.wifi_pswd = PWD;
    bool need_deallocation;
    ext_get_endpoint_public_key(&props.kaa_public_key, &props.kaa_public_key_length, &need_deallocation);
    kaa_error_t error = kaa_client_create(&kaa_client, &props);
    if (error) {
        debug("Error initialising Kaa client, error code %d\r\n", error);
        return;
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
}

void loop() {

	if (kaa_client) {
		debug("Starting Kaa client, to stop press \'S\'\r\n");
		kaa_error_t error = kaa_client_start(kaa_client, process, (void*)kaa_client, 10);
		if (error) {
			debug("Error running Kaa client, code %d\r\n", error);
		}
		kaa_client_destroy(kaa_client);
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
    static char public_key[] = { 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -57, -2, -62, -80, -64, 29, 29, -80, -15, -125, 95, -55, -94, -62, 105, -70, 46, 72, 53, -42, -112, 26, -95, -21, -95, -69, 121, -43, -82, 119, 0, 95, -10, 56, -96, -17, -80, -20, 59, -60, -3, -10, 56, -39, -89, 64, 58, 10, 111, 119, 14, -51, 20, 28, -120, -100, 109, 67, 99, 39, 10, 104, 14, -94, 41, 63, 78, 46, -9, -79, 108, -71, 118, -76, 83, -121, 10, 64, 26, -90, 53, -123, -113, 50, 54, 26, -2, 106, -97, -12, 40, -110, 68, 110, -22, 95, -114, 53, -52, -7, 72, 125, -44, 37, 42, 116, -88, 53, 2, -20, -66, 17, 2, -81, -5, -83, 69, 74, 97, 57, 37, 90, -60, -116, 18, 13, 67, 48, -64, -104, -40, 14, -108, -42, -21, -85, 59, 26, -30, -118, -2, 10, 12, -127, 74, -21, -55, 76, -70, -25, 74, -47, 125, 89, -62, -109, -53, 43, -41, -88, 19, 126, -96, -28, -30, 52, 57, 114, -103, -14, -30, -31, -82, 44, -104, 56, -112, 29, -20, -17, -78, 7, 91, 106, 118, -9, -15, -68, -51, -34, -93, 91, 41, 61, -119, -62, -46, 96, 40, -24, -47, -44, 21, 22, 75, 9, -48, -40, -111, 17, -66, 87, 108, 32, -30, 116, -87, -71, -120, -122, -90, 52, 0, -1, -25, -2, -85, 101, -91, 99, -82, 126, 20, -23, -106, 115, -69, 118, 38, -9, -59, -127, -126, -27, 119, -90, -104, 69, -48, -5, -93, 110, 57, 58, -23, 91, 2, 3, 1, 0, 1 };
    static const size_t public_key_size = sizeof(public_key) / sizeof(char);

    *buffer = public_key;
    *buffer_size = public_key_size;
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
	delay(1000);
	digitalWrite(ESP8266_RST, HIGH);
	delay(2000);
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
