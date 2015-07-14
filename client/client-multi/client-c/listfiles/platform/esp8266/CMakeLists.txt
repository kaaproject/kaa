#change this to path where you've installed xtensa toolchain
set(ESP_TOOLCHAIN_BASE /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin)

set (ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

set(CMAKE_SYSTEM_NAME FreeRTOS)

set(CMAKE_C_COMPILER ${ESP_TOOLCHAIN_BASE}/xtensa-lx106-elf-gcc)

set(CMAKE_LIBRARY_PATH ${ESP_SDK_BASE}/lib/)

set(ESP8266_INCDIRS 
    ${ESP_SDK_BASE}/extra_include ${ESP_SDK_BASE}/include
    ${ESP_SDK_BASE}/include/lwip ${ESP_SDK_BASE}/include/lwip/ipv4
    ${ESP_SDK_BASE}/include/lwip/ipv6 ${ESP_SDK_BASE}/include/espressif/
    ${ESP_TOOLCHAIN_BASE}/../lib/gcc/xtensa-lx106-elf/4.8.2/include/
    )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-implicit-function-declaration -DESP8266_PLATFORM -Os -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals  -D__ets__ -DICACHE_FLASH -ffunction-sections")

set(ESP8266_SRC_FOLDER ${KAA_SRC_FOLDER}/platform-impl/esp8266)

set(ESP8266_SRC 
    ${ESP8266_SRC_FOLDER}/esp8266_logger.c
    ${ESP8266_SRC_FOLDER}/esp8266_kaa_client.c
    ${ESP8266_SRC_FOLDER}/esp8266_configuration_persistence.c
    ${ESP8266_SRC_FOLDER}/esp8266_status.c
    ${ESP8266_SRC_FOLDER}/esp8266_key_utils.c
    #${ESP8266_SRC_FOLDER}/sha.c
    ${KAA_SRC_FOLDER}/platform-impl/sha.c
    ${KAA_SRC_FOLDER}/platform-impl/sha1.c
    )

