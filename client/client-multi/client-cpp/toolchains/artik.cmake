include(CMakeForceCompiler)

#Set the target parameters
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION arm-linux)
set(CMAKE_SYSTEM_PROCESSOR arm)

set(artik_sdk_root /opt/gcc-linaro-arm-linux-gnueabihf)
set(cross_compiler_sysroot ${artik_sdk_root})

CMAKE_FORCE_C_COMPILER(${cross_compiler_sysroot}/bin/arm-linux-gnueabihf-gcc GNU)

set(CMAKE_SYSROOT ${artik_sdk_root}/arm-linux-gnueabihf/libc)
set(CMAKE_FIND_ROOT_PATH ${CMAKE_SYSROOT})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)


SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os -g3 -Wall --sysroot=${CMAKE_SYSROOT}"  CACHE STRING "" FORCE)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os -g3 -Wall --sysroot=${CMAKE_SYSROOT}"  CACHE STRING "" FORCE)

set(CMAKE_SHARED_LIBRARY_LINK_C_FLAGS "")
set(CMAKE_SHARED_LIBRARY_LINK_CXX_FLAGS "")

INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/arm-linux-gnueabihf/include/c++/4.8.2)
INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/arm-linux-gnueabihf/libc/usr/include)
INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/lib/gcc/arm-linux-gnueabihf/4.8.2/include)
