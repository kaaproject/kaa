include(CMakeForceCompiler)

#change this to path where you've installed xtensa toolchain
set(ESP_TOOLCHAIN_BASE /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin)

set (ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

CMAKE_FORCE_C_COMPILER(${ESP_TOOLCHAIN_BASE}/xtensa-lx106-elf-gcc GNU)

set(CMAKE_LIBRARY_PATH ${ESP_SDK_BASE}/lib/)

set(ESP8266_INCDIRS 
    ${ESP_SDK_BASE}/extra_include ${ESP_SDK_BASE}/include
    ${ESP_SDK_BASE}/include/lwip ${ESP_SDK_BASE}/include/lwip/ipv4
    ${ESP_SDK_BASE}/include/lwip/ipv6 ${ESP_SDK_BASE}/include/espressif/
    ${ESP_TOOLCHAIN_BASE}/../lib/gcc/xtensa-lx106-elf/4.8.2/include/
    )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-implicit-function-declaration -Os -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections")

