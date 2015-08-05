if(NOT DEFINED KAA_TOOLCHAIN_PATH)
  set(KAA_ARM_TOOLCHAIN "/opt/kaa/gcc-arm-none-eabi")
  set(CC32XX_SDK "/opt/kaa/cc3200-sdk")
else()
  set(KAA_ARM_TOOLCHAIN "${KAA_TOOLCHAIN_PATH}/gcc-arm-none-eabi")
  set(CC32XX_SDK "${KAA_TOOLCHAIN_PATH}/cc3200_sdk")
endif()

set(CMAKE_C_COMPILER   "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-gcc")
set(CMAKE_CXX_COMPILER "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-g++")
set(CMAKE_AR           "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ar")
set(CMAKE_LINKER       "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ld")
set(CMAKE_OBJCOPY      "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-objcopy")
set(CMAKE_OBJDUMP      "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-objdump")
set(CMAKE_NM           "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-nm")
set(CMAKE_RANLIB       "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-ranlib")
set(CMAKE_STRIP        "${KAA_ARM_TOOLCHAIN}/bin/arm-none-eabi-strip")

set(CMAKE_C_FLAGS "-mthumb -mcpu=cortex-m4 -ffunction-sections -fdata-sections -MD -std=c99 -g -O0 -static"  CACHE STRING "" FORCE)
set(CMAKE_A_FLAGS "-mthumb -mcpu=cortex-m4 -MD -static"  CACHE STRING "" FORCE)

include_directories(${CC32XX_SDK}/inc)
include_directories(${CC32XX_SDK}/driverlib)
include_directories(${CC32XX_SDK}/oslib)
include_directories(${CC32XX_SDK}/simplelink)
include_directories(${CC32XX_SDK}/simplelink/include)
include_directories(${CC32XX_SDK}/simplelink/source)
include_directories(${CC32XX_SDK}/example/common)

add_library(driver STATIC IMPORTED)
set_property(TARGET driver PROPERTY IMPORTED_LOCATION ${CC32XX_SDK}/driverlib/gcc/exe/libdriver.a)

add_library(simplelink_nonos STATIC IMPORTED)
set_property(TARGET simplelink_nonos PROPERTY IMPORTED_LOCATION ${CC32XX_SDK}/simplelink/gcc/exe/libsimplelink_nonos.a)

add_definitions(-DCC32XX_PLATFORM)


set(KAA_THIRDPARTY_LIBRARIES driver simplelink_nonos)

set(KAA_BUILD_STATIC kaac_s)

set(CC32XX_RSA_KEY ../src/kaa/platform-impl/cc32xx/cc32xx_rsa_key.h)

execute_process(COMMAND rm ${CC32XX_RSA_KEY})
execute_process(COMMAND java -jar ../tools/pub_key_generator.jar ${CC32XX_RSA_KEY})
