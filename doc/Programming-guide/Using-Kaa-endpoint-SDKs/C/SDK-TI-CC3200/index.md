---
layout: page
title: Texas Instruments CC3200
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to build applications for Texas Instruments CC3200 LaunchPad (further, CC3200) based on the Kaa C SDK.

# Installing necessary components for Linux (Ubuntu)

Before building the C SDK for the CC3200 platform on Linux, you need to perform the next installation steps.

1. Install the GNU ARM toolchain: [gcc-arm-none-eabi](https://launchpad.net/gcc-arm-embedded)

        wget https://launchpad.net/gcc-arm-embedded/4.9/4.9-2015-q2-update/+download/gcc-arm-none-eabi-4_9-2015q2-20150609-linux.tar.bz2
        tar -xjf gcc-arm-none-eabi-4_9-2015q2-20150609-linux.tar.bz2
        sudo mkdir /opt/kaa
        sudo chown <username>:<usergroup> /opt/kaa
        mv gcc-arm-none-eabi-4_9-2015q2 /opt/kaa/gcc-arm-none-eabi
1. Install and configure OpenOCD, which is needed for running and debugging applications.
    1. Install OpenOCD.

            sudo apt-get install openocd
    1. Add a rule file.

            cd /etc/udev/rules.d/
            sudo nano 98-usbftdi.rules
    1. Write the following code into the added rule file and then save the file.

            SUBSYSTEM=="usb", ATTRS{idVendor}=="0451", ATTRS{idProduct}=="c32a", MODE="0660", GROUP="dialout",
            RUN+="/sbin/modprobe ftdi-sio" RUN+="/bin/sh -c '/bin/echo 0451 c32a > /sys/bus/usb-serial/drivers/ftdi_sio/new_id'"
    1. Reload the rules.

            sudo udevadm control --reload-rules
    1. To use OpenOCD as a regular user, add yourself to the dialout group.

            sudo usermod -a -G dialout <username>
    1. Log out and log in to finish the process.
    NOTE: The board should be enumerated as `/dev/ttyUSB{0,1}`. Use ttyUSB1 for UART.
1. Install CC3200 SDK.
    1. Install [Wine](https://www.winehq.org/).

            sudo apt-get install wine
    1. [Download](http://www.ti.com/tool/cc3200sdk) CC3200 SDK.
    1. Unpack CC3200 SDK.
    1. Change the configuration file for the debug interface.
        In `/opt/kaa/cc3200-sdk/tools/gcc_scripts/cc3200.cfg` replace the following few lines:

            interface ft2232
            ft2232_layout luminary_icdi
            ft2232_device_desc "USB <-> JTAG/SWD"
            ft2232_vid_pid 0x0451 0xc32a

        with

            interface ftdi
            ftdi_device_desc "USB <-> JTAG/SWD"
            ftdi_vid_pid 0x0451 0xc32a
            ftdi_layout_init 0x00a8 0x00eb
            ftdi_layout_signal nSRST -noe 0x0020
    1. Edit GDB configuration.

            sed -i 's#-f cc3200.cfg#-f /opt/kaa/cc3200-sdk/tools/gcc_scripts/cc3200.cfg#g' /opt/kaa/cc3200-sdk/tools/gcc_scripts/gdbinit


# Installing necessary components for Windows

Before building the C SDK for the CC3200 platform on Windows, you need to perform the following installation.

1. Install [Cygwin](https://www.cygwin.com/) with the additional following packages:
    * `Archive/unzip`
    * `Archive/zip`
    * `Devel/autoconf`
    * `Devel/automake`
    * `Devel/libtool`
    * `Devel/subversion` (Note: if using TortoiseSVN/Windows7, skip this file)
    * `Devel/make`
    * `Devel/gcc-core`
    * `Devel/gcc-g++`
    * `Devel/mingw-gcc-core`
    * `Devel/mingw-gcc-g++`
    * `Devel/mingw-runtime`


    Refer to [SO question page](http://superuser.com/questions/304541/how-to-install-new-packages-on-cygwin) for details how to install packages on Cygwin.
1. Install the GNU ARM toolchain: [gcc-arm-none-eabi](https://launchpad.net/gcc-arm-embedded) to the `opt\kaa` directory under the Cygwin root folder (default is `C:\cygwin`).
1. Install [CC3200 SDK](http://www.ti.com/tool/cc3200sdk) to the `opt\kaa` (if the directory doesn't exist, create it) directory under the Cygwin root folder.
1. Install [cmake](http://www.cmake.org/) and add it bin directory to the system environment.

To enable debugging for your CC3200 applications, you will also need to build OpenOCD as described in [the official CC3200-Getting_Started_Guide][cc3200-getting-started-guide] (item 3.3.3).

For more information, please refer to [the official CC3200 Getting Started Guide][cc3200-getting-started-guide].

[cc3200-getting-started-guide]: http://www.ti.com/lit/ug/swru376d/swru376d.pdf

# Creating applications based on C SDK

Before creating applications based on the C SDK, you should obtain the C SDK and build a static library from it.
To do so, generate the [C SDK in Admin UI]({{root_url}}/Administration-guide/Tenants-and-applications-management/#generating-endpoint-sdk), then extract the archive.

## Building C SDK on Linux

Change directory to where SDK was unpacked and execute the following:

```
mkdir -p build
cd build
cmake -DKAA_PLATFORM=cc32xx -DCMAKE_TOOLCHAIN_FILE=../toolchains/cc32xx.cmake ..
make
```

Refer to [C SDK Linux page]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for more details.

## Building C SDK on Windows

Open the Cygwin terminal and execute the following:

```
mkdir -p build
cd build
cmake.exe -G "Unix Makefiles" -DKAA_PLATFORM=cc32xx -DCC32XX_TOOLCHAIN_PATH=c:/cygwin/opt/kaa -DCMAKE_TOOLCHAIN_FILE=../toolchains/cc32xx.cmake ..
make
```

# Example

To quickly start with the Kaa IoT platform, you can download one of the Kaa demo applications from the [Kaa Sandbox]({{root_url}}/Getting-started/) and run it on the CC3200 board. We recommend you to start with the ConfigurationDemo.

Connect CC3200 LaunchPad to your PC through a micro-USB connector and execute the following in your terminal:

```
tar -xzf configuration_demo.tar.gz
cd CConfigurationDemo
./build.sh build
```

You will be asked for desired platform. Type `cc32xx` into the console.

```
Please enter a target (default is x86-64):
cc32xx
```

To launch the application, execute the following:

```
/opt/kaa/gcc-arm-none-eabi/bin/arm-none-eabi-gdb -x /opt/kaa/cc3200-sdk/tools/gcc_scripts/gdbinit build/demo_client.afx
```

NOTE: If you want to see the debug output in the terminal, make sure to get connected to `/dev/ttyUSB{0,1}.`

# Flashing (Windows only)

Jumpers on the CC3200 board should be connected as shown below.

![Jumpers](attach/jumpers_debug_mode_400.png)

To run an application, remove SOP2 and J8 jumpers, and then connect J2 and J3 jumpers.

For more information, see the official [UniFlash Quick Start Guide](http://processors.wiki.ti.com/index.php/CC31xx_%26_CC32xx_UniFlash_Quick_Start_Guide).
