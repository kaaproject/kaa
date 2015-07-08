#change this to path where you've installed xtensa toolchain
set(ESP_TOOLCHAIN_DIR /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin/)

set (ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

set(CMAKE_SYSTEM_NAME FreeRTOS)

set(CMAKE_C_COMPILER ${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-gcc)

set(CMAKE_LIBRARY_PATH ${ESP_SDK_BASE}/lib/)

set(ESP8266_INCDIRS ${ESP_SDK_BASE}/extra_include ${ESP_SDK_BASE}/include
                    ${ESP_SDK_BASE}/include/lwip ${ESP_SDK_BASE}/include/lwip/ipv4
                    ${ESP_SDK_BASE}/include/lwip/ipv6 ${ESP_SDK_BASE}/include/espressif/)

set(CMAKE_C_FLAGS "-DESP8266_PLATFORM -Os -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals  -D__ets__ -DICACHE_FLASH")

