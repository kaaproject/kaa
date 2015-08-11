include(CMakeForceCompiler)

if(NOT DEFINED ESPRESSIF_HOME)
    set(ESPRESSIF_HOME /opt/Espressif)
endif()

set(ESP_TOOLCHAIN_DIR ${ESPRESSIF_HOME}/crosstool-NG/builds/xtensa-lx106-elf/bin)

set (ESP_SDK_BASE ${ESPRESSIF_HOME}/esp-rtos-sdk)

CMAKE_FORCE_C_COMPILER(${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-gcc GNU)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DESP8266_PLATFORM -Os -Wpointer-arith -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -D__ets__ -DICACHE_FLASH")

