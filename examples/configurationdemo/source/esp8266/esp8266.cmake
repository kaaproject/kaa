include(CMakeForceCompiler)

set(ESP_TOOLCHAIN_DIR /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin)

set (ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

CMAKE_FORCE_C_COMPILER(${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-gcc GNU)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DESP8266_PLATFORM -Os -Wpointer-arith -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -D__ets__ -DICACHE_FLASH")

