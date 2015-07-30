set(cross_compiler_tools "/home/karnil/work/arm-toolchain/gcc-arm-none-eabi-4_9-2015q2")
set(cc32xx_sdk "/home/karnil/work/cc3200/sdk")

set(CMAKE_C_COMPILER   "${cross_compiler_tools}/bin/arm-none-eabi-gcc")
set(CMAKE_CXX_COMPILER "${cross_compiler_tools}/bin/arm-none-eabi-g++")
set(CMAKE_AR           "${cross_compiler_tools}/bin/arm-none-eabi-ar")
set(CMAKE_LINKER       "${cross_compiler_tools}/bin/arm-none-eabi-ld")
set(CMAKE_OBJCOPY      "${cross_compiler_tools}/bin/arm-none-eabi-objcopy")
set(CMAKE_OBJDUMP      "${cross_compiler_tools}/bin/arm-none-eabi-objdump")
set(CMAKE_NM           "${cross_compiler_tools}/bin/arm-none-eabi-nm")
set(CMAKE_RANLIB       "${cross_compiler_tools}/bin/arm-none-eabi-ranlib")
set(CMAKE_STRIP        "${cross_compiler_tools}/bin/arm-none-eabi-strip")

set(CMAKE_C_FLAGS "-mthumb -mcpu=cortex-m4 -ffunction-sections -fdata-sections -MD -std=c99 -g -O0 -static"  CACHE STRING "" FORCE)
set(CMAKE_A_FLAGS "-mthumb -mcpu=cortex-m4 -MD -static"  CACHE STRING "" FORCE)

include_directories(${cc32xx_sdk}/inc)
include_directories(${cc32xx_sdk}/lib/driverlib)
include_directories(${cc32xx_sdk}/lib/oslib)
include_directories(${cc32xx_sdk}/lib/simplelink)
include_directories(${cc32xx_sdk}/lib/simplelink/include)
include_directories(${cc32xx_sdk}/lib/simplelink/source)
include_directories(${cc32xx_sdk}/example/common)

add_library(driver STATIC IMPORTED)
set_property(TARGET driver PROPERTY IMPORTED_LOCATION ${cc32xx_sdk}/lib/driverlib/gcc/exe/libdriver.a)

add_library(simplelink_nonos STATIC IMPORTED)
set_property(TARGET simplelink_nonos PROPERTY IMPORTED_LOCATION ${cc32xx_sdk}/lib/simplelink/gcc/exe/libsimplelink_nonos.a)

add_definitions(-DCC32XX_PLATFORM)

#add_definitions(-DDEFAULT_USER_VERIFIER_TOKEN="35913006593875148785")


set(KAA_THIRDPARTY_LIBRARIES driver simplelink_nonos)

set(KAA_BUILD_STATIC kaac_s)

set(CC32XX_RSA_KEY ../src/kaa/platform-impl/cc32xx/cc32xx_rsa_key.h)

execute_process(COMMAND rm ${CC32XX_RSA_KEY})
execute_process(COMMAND java -jar ../listfiles/platform/cc32xx/pub_key_generator.jar ${CC32XX_RSA_KEY})
