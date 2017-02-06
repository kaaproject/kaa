---
layout: page
title: ESP8266
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to install [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) and cross-compile it the [ESP8266 platform](https://espressif.com/en/products/hardware/esp8266ex/overview).

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Device:** ESP8266-01 & [NodeMCU](http://www.nodemcu.com/index_en.html)
{:.note}

## Connecting ESP8266

To flash ESP8266 chip, you need to connect it to your PC.
That may differ depending on the module you use.
This guide covers instructions for ESP8266-01 and NodeMCU modules.

### ESP8266-01

A 3.3V USB-to-TTL connector is required to connect ESP8266-01 to PC.
There are two boot modes for ESP8266: flash mode and run mode.
The table below summarizes wiring scheme for both boot modes.

<ul class="nav nav-tabs">
    <li class="active"><a data-toggle="tab" href="#Flash">Flash mode</a></li>
    <li><a data-toggle="tab" href="#Run">Run mode</a></li>
</ul>

<div class="tab-content">
<div id="Flash" class="tab-pane fade in active" markdown="1" >
| ESP8266-01 | USB-to-TTL |
|:----------:|:----------:|
| URXD       | TXD        |
| UTXD       | RXD        |
| CH_PD      | 3.3V       |
| GND        | GND        |
| VCC        | 3.3V       |
| GPIO0      | GND        |
| GPIO2      | 3.3V       |

</div><div id="Run" class="tab-pane fade" markdown="1" >
| ESP8266-01 | USB-to-TTL |
|:----------:|:----------:|
| URXD       | TXD        |
| UTXD       | RXD        |
| CH_PD      | 3.3V       |
| GND        | GND        |
| VCC        | 3.3V       |

</div></div>

### NodeMCU

Connect your NodeMCU module using a micro-USB cable.

## Prerequisites

Before developing [Kaa applications]({{root_url}}Glossary/#kaa-application) for ESP8266, you need to install some dependencies.
To do that:

1. Run the following command:

   ```bash
   sudo apt-get install autoconf libtool libtool-bin bison build-essential gawk git gperf flex texinfo libncurses5-dev libc6-dev-amd64 python-serial libexpat-dev python-setuptools
   ```

2. Set the `ESPRESSIF_HOME` variable.
This variable is used throughout the installation process and specifies the directory where ESP8266 SDK and toolchain will be stored.

   ```bash
   export ESPRESSIF_HOME=/opt/Espressif/
   ```

3. Install toolchain.

   ```bash
   cd $ESPRESSIF_HOME
   git clone -b lx106-g++ git://github.com/jcmvbkbc/crosstool-NG.git
   cd crosstool-NG
   ./bootstrap && ./configure --prefix=$(pwd)
   make
   sudo make install
   ./ct-ng xtensa-lx106-elf
   ./ct-ng build
   ```
   
4. In the `.bashrc` file, specify the path to the toolchain binary files.

   ```bash
   echo "export PATH=$ESPRESSIF_HOME/crosstool-NG/builds/xtensa-lx106-elf/bin:\$PATH" >> ~/.bashrc
   ```

5. Install ESP8266 RTOS SDK.

   ```bash
   cd $ESPRESSIF_HOME
   export ESP_SDK_HOME=$ESPRESSIF_HOME/esp-rtos-sdk
   git clone https://github.com/espressif/esp_iot_rtos_sdk.git $ESP_SDK_HOME
   cd $ESP_SDK_HOME
   git checkout 169a436ce10155015d056eab80345447bfdfade5
   wget -O lib/libhal.a https://github.com/esp8266/esp8266-wiki/raw/master/libs/libhal.a
   cd $ESP_SDK_HOME/include/lwip/arch
   sed -i 's:#include "c_types.h"://#include "c_types.h":' $ESP_SDK_HOME/include/lwip/arch/cc.h
   ```

6. Install `esptool.py`.

   ```bash
   cd $ESPRESSIF_HOME
   git clone https://github.com/espressif/esptool.git
   cd esptool
   python setup.py install --user
   ```

## Creating Kaa applications

After you successfully installed the dependencies, you can proceed to application development.
See [application code example]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/).

### Directory structure

The following directory structure is used as an example:

```
CMakeLists.txt
driver/
    uart.h
    uart.c
ld/
    eagle.app.v6.ld
    eagle.rom.addr.v6.ld
kaa/
    <put Kaa SDK here>
user/
    user_main.c
src/
    kaa_demo.c
```

* [CMakeLists.txt]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/CMakeLists.txt) is a CMake script (see below).
* [driver/uart.c]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/driver/uart.c) and [driver/uart.h]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/driver/uart.h) files implement driver for ESP8266 UART interface.
* The [ld/]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/ld) directory contains two linker scripts required for ESP8266 applications.
* The `kaa/` directory is where you put the generated Kaa C SDK tarball and unpack it.

  ```bash
  mkdir kaa && cd kaa
  tar zxf kaa-c*.tar.gz
  ```

* [user/user_main.c]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/user/user_main.c) contains ESP8266 application entry ponit (`user_init()` function)
and performs ESP8266-specific initizalizations (e.g. initialize UART).
* [src/kaa_demo.c]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/src/kaa_demo.c) is a platofrm-independent source file with minimum Kaa code.

### Code

Each ESP8266 application starts its execution from the `user_init()` function.
Use the below code to initialize UART and set its baudrate to 115200 baud.

```c
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

#include "uart.h"

void user_init(void)
{
    uart_init_new();
    UART_SetBaudrate(UART0, 115200);
    UART_SetPrintPort(UART0);
```

Start a system task in `user_init()`.

```c
    portBASE_TYPE error = xTaskCreate(main_task, "main_task",
            512, NULL, 2, NULL );
    if (error < 0) {
        printf("Error creating main_task! Error code: %ld\r\n", error);
    }
}
```

In scope of this example, `main_task()` calls `main()` function.

```c
static void main_task(void *pvParameters)
{
    (void)pvParameters;
    main();
    for (;;);
}
```

The `main()` function is defined in [src/kaa_demo.c]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/src/kaa_demo.c) and starts minimum required [Kaa client]({{root_url}}Glossary/#kaa-client) using Kaa C SDK.

```c
#include <stddef.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>

static void loop_fn(void *context)
{
    printf("Hello, Kaa!\r\n");

    kaa_client_stop(context);
}

int main(void)
{
    printf("Initializing Kaa client\r\n");

    kaa_client_t *kaa_client;

    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    if (error_code) {
        printf("Failed to create Kaa client\r\n");
        return 1;
    }

    error_code = kaa_client_start(kaa_client, loop_fn, kaa_client, 0);
    if (error_code) {
        printf("Failed to start Kaa main loop\r\n");
        return 1;
    }

    kaa_client_destroy(kaa_client);
    return 0;
}
```

### Build system

The Kaa C SDK uses [CMake](https://cmake.org/) build system generator.
You can use other build systems, but CMake is recommended for better integration of you application's build system with Kaa C SDK.

Below is an example how to configure the `CMakeLists.txt` file for the application.

1. Specify minimum CMake version required and the project name.

   ```bash
   cmake_minimum_required(VERSION 3.0.2)
   project(kaa_demo C)
   ```

2. Set CMake configuration variables to be used by Kaa C SDK for build configuration.
You can disable all SDK features since you will not use them in scope of this example application.

   ```bash
   set(KAA_WITH_EXTENSION_CONFIGURATION OFF)
   set(KAA_WITH_EXTENSION_EVENT OFF)
   set(KAA_WITH_EXTENSION_NOTIFICATION OFF)
   set(KAA_WITH_EXTENSION_LOGGING OFF)
   set(KAA_WITH_EXTENSION_USER OFF)
   set(KAA_WITH_EXTENSION_PROFILE OFF)
   ```

3. Add Kaa SDK subdirectory to build it together with the application.

   ```bash
   add_subdirectory(kaa)
   ```

4. Create a static library with demo source files.

   ```bash
   add_library(kaa_demo_s STATIC user/user_main.c driver/uart.c src/kaa_demo.c)
   ```

5. Enable c99 standard.

   ```bash
   set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")
   ```

6. Add required include directories to compiler search path.

   ```bash
   if(NOT DEFINED ESP_RTOS_SDK)
   set(ESP_RTOS_SDK /opt/Espressif/esp-rtos-sdk)
   endif()

   # specify include directories
   target_include_directories(kaa_demo_s PUBLIC driver)
   target_include_directories(kaa_demo_s PUBLIC .)
   target_include_directories(kaa_demo_s PUBLIC
                           ${ESP_RTOS_SDK}/extra_include
                           ${ESP_RTOS_SDK}/include
                           ${ESP_RTOS_SDK}/include/lwip
                           ${ESP_RTOS_SDK}/include/lwip/ipv4
                           ${ESP_RTOS_SDK}/include/lwip/ipv6
                           ${ESP_RTOS_SDK}/include/espressif/
                           )
   ```

7. Specify libraries for CMake to link with -- the libraries from ESP8266 RTOS SDK, linker script, and Kaa SDK.
Add [ld/]({{github_url}}/doc/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/attach/esp8266-sample/ld/) directory to linker search paths to use linker scripts.

   ```bash
   exec_program(xtensa-lx106-elf-gcc .
            ARGS -print-libgcc-file-name
            OUTPUT_VARIABLE ESP8266_LIBGCC
            )

   link_directories(${CMAKE_CURRENT_SOURCE_DIR}/ld)

   target_link_libraries(kaa_demo_s PUBLIC
                      kaac
                      ${ESP_RTOS_SDK}/lib/libfreertos.a
                      ${ESP_RTOS_SDK}/lib/libhal.a
                      ${ESP_RTOS_SDK}/lib/libpp.a
                      ${ESP_RTOS_SDK}/lib/libphy.a
                      ${ESP_RTOS_SDK}/lib/libnet80211.a
                      ${ESP_RTOS_SDK}/lib/libwpa.a
                      ${ESP_RTOS_SDK}/lib/liblwip.a
                      ${ESP_RTOS_SDK}/lib/libmain.a
                      ${ESP_RTOS_SDK}/lib/libssl.a
                      ${ESP_RTOS_SDK}/lib/libhal.a
                      ${ESP8266_LIBGCC}
                      -Teagle.app.v6.ld
                      )
   ```

    This is required due to ESP8266 specific requirements regarding linked executable files.
    The `blank.c` file is a placeholder for CMake's `add_executable()`.
    All the code (Kaa SDK, ESP8266 SDK, and the demo application) is compiled as static libraries and linked into an executable file.

   ```CMake
   file(WRITE ${CMAKE_BINARY_DIR}/blank.c "")
   add_executable(kaa_demo ${CMAKE_BINARY_DIR}/blank.c)
   ```

8. Link your application with the demo library.

   ```bash
   target_link_libraries(kaa_demo kaa_demo_s)
   ```

### Build application

To build your application using CMake, run the commands below.

```bash
mkdir build
cd build
cmake .. \
    -DCMAKE_TOOLCHAIN_FILE=../kaa/toolchains/esp8266.cmake \
    -DKAA_PLATFORM=esp8266 \
    -DBUILD_TESTING=OFF \
    -DCMAKE_BUILD_TYPE=MinSizeRel \
    -DKAA_MAX_LOG_LEVEL=3
make
```

See [Kaa C SDK page]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C) for available options.

## Flashing
Once the application is built, a `kaa_demo` ELF executable file will be stored in the `build` directory.

To flash the application to ESP8266:

1. Make firmware images.
Use `esptool.py` tool to produce two binaries -- `kaa_demo-0x00000.bin` and `kaa_demo-0x40000.bin` to be flashed to 0x00000 and 0x40000 flash addresses respectively.

   ```bash
   esptool.py elf2image build/kaa_demo
   ```

2. Write these binary files to flash memory.
Prior to that, make sure you have enabled flash mode on ESP8266 chip (see [Connecting ESP8266](#connecting-esp8266)).
If the connection is performed correctly, run this command.

   ```bash
   sudo esptool.py write_flash 0x00000 build/kaa_demo-0x00000.bin 0x40000 build/kaa_demo-0x40000.bin
   ```

The flashing process will take some time.
When the firmware starts, `Hello, Kaa!` message will be displayed.