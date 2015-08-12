#
# Copyright 2015 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if(NOT DEFINED KAA_TOOLCHAIN_PATH)
  set(KAA_ARM_TOOLCHAIN "/opt/kaa/gcc-arm-none-eabi")
  set(CC32XX_SDK "/opt/kaa/cc3200-sdk")
else()
  set(KAA_ARM_TOOLCHAIN "${KAA_TOOLCHAIN_PATH}/gcc-arm-none-eabi")
  set(CC32XX_SDK "${KAA_TOOLCHAIN_PATH}/cc3200_sdk")
endif()

message("## Kaa arm toolchain path: " ${KAA_ARM_TOOLCHAIN})
message("## CC32XX SDK path: " ${CC32XX_SDK})

set(CMAKE_C_COMPILER   "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-gcc")
set(CMAKE_CXX_COMPILER "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-g++")
set(CMAKE_AR           "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ar")
set(CMAKE_LINKER       "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ld")
set(CMAKE_OBJCOPY      "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-objcopy")
set(CMAKE_OBJDUMP      "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-objdump")
set(CMAKE_NM           "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-nm")
set(CMAKE_RANLIB       "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ranlib")
set(CMAKE_STRIP        "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-strip")

set(CMAKE_C_FLAGS "-mthumb -mcpu=cortex-m4 -ffunction-sections -fdata-sections -MD -O0 "  CACHE STRING "" FORCE)

set(CMAKE_SHARED_LIBRARY_LINK_C_FLAGS)    # remove -rdynamic
set(CMAKE_EXE_LINK_DYNAMIC_C_FLAGS)       # remove -Wl,-Bdynamic

add_definitions(-Dgcc -DCC32XX -DUSER_INPUT_ENABLE)

include_directories(${CC32XX_SDK}/inc)
include_directories(${CC32XX_SDK}/driverlib)
include_directories(${CC32XX_SDK}/simplelink)
include_directories(${CC32XX_SDK}/simplelink/include)
include_directories(${CC32XX_SDK}/simplelink/source)
include_directories(${CC32XX_SDK}/example/common)

set(LIB_KAA ${CMAKE_CURRENT_SOURCE_DIR}/build/libkaac_s.a)
set(LIB_DRIVER ${CC32XX_SDK}/driverlib/gcc/exe/libdriver.a)
set(LIB_SIMPLELINK_NONOS ${CC32XX_SDK}/simplelink/gcc/exe/libsimplelink_nonos.a)
set(LIB_GCC ${KAA_ARM_TOOLCHAIN}/lib/gcc/arm-none-eabi/4.9.3/armv7e-m/libgcc.a)
set(LIB_C ${KAA_ARM_TOOLCHAIN}/arm-none-eabi/lib/armv7e-m/libc.a)
set(LIB_M ${KAA_ARM_TOOLCHAIN}/arm-none-eabi/lib/armv7e-m/libm.a)

set(APP_LIBS ${LIB_KAA} ${LIB_SIMPLELINK_NONOS} ${LIB_DRIVER} ${LIB_M} ${LIB_C} ${LIB_GCC})

set (SAMPLE_SOURCE_FILES
            src/kaa_demo.c           
            platforms/${KAA_PLATFORM}/${KAA_PLATFORM}_support.c
	    ${CC32XX_SDK}/example/common/uart_if.c
	    ${CC32XX_SDK}/example/common/udma_if.c
            ${CC32XX_SDK}/example/common/gpio_if.c
	    ${CC32XX_SDK}/example/common/startup_gcc.c 
    )

set(CMAKE_C_LINK_EXECUTABLE "${CMAKE_LINKER} -T ${CMAKE_CURRENT_SOURCE_DIR}/../platforms/${KAA_PLATFORM}/app.ld --entry ResetISR --gc-sections -o ${APP_NAME}.afx <OBJECTS> <LINK_LIBRARIES>")
