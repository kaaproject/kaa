#change this to path where you've installed xtensa toolchain
set(ESP_TOOLCHAIN_DIR /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin/)

set (ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

set(CMAKE_SYSTEM_NAME FreeRTOS)

set(CMAKE_C_COMPILER ${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-gcc)

set(CMAKE_LIBRARY_PATH ${ESP_SDK_BASE}/lib/)
message("${CMAKE_INCLUDE_PATH}")

set(CMAKE_C_FLAGS "-I${ESP_SDK_BASE}/extra_include -I${ESP_SDK_BASE}/include -I${ESP_SDK_BASE}/include/lwip  -I${ESP_SDK_BASE}/include/lwip/ipv4  -I${ESP_SDK_BASE}/include/lwip/ipv6 -DESP8266_PLATFORM -Os -O2 -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals  -D__ets__ -DICACHE_FLASH")

