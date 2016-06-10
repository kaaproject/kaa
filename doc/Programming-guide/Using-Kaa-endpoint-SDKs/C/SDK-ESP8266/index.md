---
layout: page
title: ESP8266
permalink: /:path/
sort_idx: 20
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

- [Introduction](#introduction)
- [Connecting ESP8266](#connecting-esp8266)
    - [ESP8266-01](#esp8266-01)
    - [NodeMCU](#nodemcu)
- [Installing requirements](#installing-requirements)
- [Writing applications](#writing-appllications)
    - [Directory structure](#directory-structure)
    - [Minimal code](#minimal-code)
    - [Build system overview](#build-system-overview)
- [Bulding](#building)
- [Flashing](#flashing)
- [What's next?](#whats-next)

## Introduction

This page will guide you through Kaa C SDK installation and compilation process for ESP8266 platform.
All steps described here were tested on *Linux Ubuntu 14.04 x86_64*.

## Connecting ESP8266

To flash ESP8266 chip you need to connect it to your PC. This may differ depending on module you use.
This guide covers instructions for ESP8266-01 and NodeMCU modules.

### ESP8266-01

To connect the ESP8266-01 to PC, a 3.3V USB-to-TTL connector is required.
There are two boot modes for ESP8266: flash mode and run mode.
The table below summarizes wiring scheme for both boot modes.

<ul class="nav nav-tabs">
    <li class="active"><a data-toggle="tab" href="#Flash">Fash Mode</a></li>
    <li><a data-toggle="tab" href="#Run">Run mode</a></li>
</ul>

<div class="tab-content">
<div id="Flash" class="tab-pane fade in active" markdown="1" >

#### Flash mode

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

#### Run mode

| ESP8266-01 | USB-to-TTL |
|:----------:|:----------:|
| URXD       | TXD        |
| UTXD       | RXD        |
| CH_PD      | 3.3V       |
| GND        | GND        |
| VCC        | 3.3V       |

</div></div>

### NodeMCU

Connecting NodeMCU is much simpler---just connect it via micro-USB cable.

## Installing requirements

Prior to developing Kaa applications for ESP8266 some requirements should be installed.
The detailed installation instructions can be found below.

1. Prerequisites

        sudo apt-get install autoconf libtool libtool-bin bison build-essential gawk git gperf flex texinfo libtool libncurses5-dev libc6-dev-amd64 python-serial libexpat-dev python-setuptools

2. Set the `ESPRESSIF_HOME` variable.
This variable will be used throughout installation process, and denotes a directory where ESP8266 SDK and toolchain will be placed.
You are free to set it to whatever you like.

            export ESPRESSIF_HOME=/opt/Espressif/

3. Install toolchain

        cd $ESPRESSIF_HOME
        git clone -b lx106 git://github.com/jcmvbkbc/crosstool-NG.git
        cd crosstool-NG
        ./bootstrap && ./configure --prefix=$(pwd) && make && sudo make install
        ./ct-ng xtensa-lx106-elf
        ./ct-ng build

4. Add path to toolchain binaries to your .bashrc:

        echo "export PATH=$ESPRESSIF_HOME/crosstool-NG/builds/xtensa-lx106-elf/bin:\$PATH" >> ~/.bashrc

4. Install ESP8266 RTOS SDK

        cd $ESPRESSIF_HOME
        export ESP_SDK_HOME=$ESPRESSIF_HOME/esp-rtos-sdk
        git clone https://github.com/espressif/esp_iot_rtos_sdk.git $ESP_SDK_HOME
        cd $ESP_SDK_HOME
        git checkout 169a436ce10155015d056eab80345447bfdfade5
        wget -O lib/libhal.a https://github.com/esp8266/esp8266-wiki/raw/master/libs/libhal.a
        cd $ESP_SDK_HOME/include/lwip/arch
        sed -i 's:#include "c_types.h"://#include "c_types.h":' $ESP_SDK_HOME/include/lwip/arch/cc.h

5. Install esptool.py

        cd $ESPRESSIF_HOME
        git clone https://github.com/RostakaGmfun/esptool.git
        cd esptool
        python setup.py install --user

## Writing applications

After successfull installation of requirements you can proceed to application development.
This section shows how to setup minimal Kaa application for ESP8266.

### Directory structure

For this basic demo, the following directory structure is used:

```
CMakeLists.txt
driver/
    uart.h
    uart.c
ld/
    eagle.app.v6.ld
    eagle.rom.addr.v6.ld
libs/kaa
    <put Kaa SDK here>
user/
    user_main.c
src/
    kaa_demo.c
```

Some notes:

* `CMakeLists.txt` is a CMake script (see below)
* `driver/uart.c` and `driver/uart.h` files implement driver for ESP8266 UART interface.
* The `ld` directory contains two linker scripts required for ESP8266 applications.
* You should put generated Kaa C SDK tarball into `libs/kaa` directory and unpack it:

        cd libs/kaa && tar zxf kaa-c*.tar.gz
* `user/user_main.c` contains ESP8266 application entry ponit (`user_init()` function)
and performs ESP8266-specific initizalizations (e.g. initialize UART).
* `src/kaa_demo.c` is a platofrm-independent source file with minimal Kaa code.

### Minimal code

Each ESP8266 application starts its execution form `user_init()` function.
Here we initialize UART and set it's baudrate to 115200 baud:

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

Next, we should start a system task in `user_init()`, since we run in FreeRTOS environment:

```c
    portBASE_TYPE error = xTaskCreate(main_task, (const signed char *)"main_task",
            512, NULL, 2, NULL );
    if (error < 0) {
        printf("Error creating main_task! Error code: %ld\r\n", error);
    }
}
```

In scope of this example, `main_task()` just calls `main()` function:

```c
static void main_task(void *pvParameters)
{
    (void)pvParameters;
    main();
    for (;;);
}
```

The `main()` function is defined in `src/kaa_demo.c` and starts minimal Kaa client using Kaa C SDK:

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

    error_code = kaa_client_start(kaa_client, loop_fn, (void*)kaa_client, 0);
    if (error_code) {
        printf("Failed to start Kaa main loop\r\n");
        return 1;
    }

    kaa_client_destroy(kaa_client);
    return 0;
}
```

### Build system overview

The Kaa C SDK makes use of CMake build system and though you can choose any build system of your preference, it is recommended to use CMake for Kaa applications as well.
This will make possible to tightly integrate your application's bulid system with Kaa SDK and use already provided ESP8266 toolchain file.

#### CMakeLists.txt

First, let's tell CMake minimum version required and project name.
```CMake
cmake_minimum_required(VERSION 3.0.2)

project(kaa_demo C)
```

We also add Kaa SDK subdirectory, so that we can build it together with application.

```CMake
add_subdirectory(libs/kaa)
```

Next, let's create a static library with demo source files.

```CMake
# Add source files
add_library(kaa_demo_s STATIC user/user_main.c driver/uart.c src/kaa_demo.c)
```

We also enable c99 standard:

```CMake
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")
```

This part adds required include directories to compiler search path.

```CMake
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

We also need to use custom linker script.
Below, two linker scripts, required for ESP8266 applications, are moved to binary directory so that linker can locate them.

```CMake
file(COPY
    ${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.rom.addr.v6.ld
    ${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld
    DESTINATION
    ${CMAKE_BINARY_DIR})
```

Next, we should tell CMake what libraries we would like to link with.
Here the required libraries from ESP8266 RTOS SDK, linker script and Kaa SDK are specified.

```CMake
exec_program(xtensa-lx106-elf-gcc .
            ARGS -print-libgcc-file-name
            OUTPUT_VARIABLE ESP8266_LIBGCC
            )

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
                      -T${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld
                      )
```

This is required due to ESP8266 specific requirements regarding linked executable.
The blank.c file is a placeholder for CMake's `add_executable()`.
All the code (Kaa SDK, ESP8266 SDK and demo) is compiled as static libraries
and linked into that executable.

```CMake
file(WRITE ${CMAKE_BINARY_DIR}/blank.c "")
add_executable(kaa_demo ${CMAKE_BINARY_DIR}/blank.c)
```

Finally, we link our application with our demo library.

```CMake
target_link_libraries(kaa_demo kaa_demo_s);
```

## Building
To actually invoke CMake, proceed as follows:

        mkdir build
        cd build
        cmake .. \
            -DCMAKE_TOOLCHAIN_FILE=../libs/kaa/toolchains/esp8266.cmake \
            -DKAA_PLATFORM=esp8266 \
            -DCMAKE_BUILD_TYPE=MinSizeRel \
            -DKAA_WITH_EXTENSION_CONFIGURATION=OFF \
            -DKAA_WITH_EXTENSION_EVENT=OFF \
            -DKAA_WITH_EXTENSION_NOTIFICATION=OFF \
            -DKAA_WITH_EXTENSION_LOGGING=OFF \
            -DKAA_WITH_EXTENSION_USER=OFF \
            -DKAA_WITH_EXTENSION_PROFILE=OFF \
            -DKAA_MAX_LOG_LEVEL=3

This will generate `Makefile` in `build` directory. To actually build application invoke `make`.

For detailed description of available options refer to [this]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C) page.

## Flashing
Once the application has been built, you'll get a `kaa_demo` ELF executable in `build` directory.
However, to flash the application to ESP8266, you should first make a firmware images.
This is done with a help of `esptool.py` tool, and produces two binaries -- `kaa_demo-0x00000.bin` and `kaa_demo-0x40000.bin`
which will be flashed to 0x00000 and 0x40000 flash addresses respectively.

        esptool.py elf2image build/kaa_demo

Now these binaries can be written to Flash memory. But first, ensure that you have enabled flash mode on ESP8266 chip (see [here](#connecting-esp8266))
If everything done correctly, invoke this command:

        sudo esptool.py write_flash 0x00000 build/kaa_demo-0x00000.bin 0x40000 build/kaa_demo-0x40000.bin

This will take some time to flash, and, eventually, when the firmware starts, you will be presented with `Hello, Kaa!` message.

## What's next?

This guide shows just the basic part of setting up Kaa on ESP8266. To actually make something useful with it, take a look at [Data Collection](TODO) demo.
