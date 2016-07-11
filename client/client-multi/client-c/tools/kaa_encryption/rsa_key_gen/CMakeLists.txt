cmake_minimum_required(VERSION 2.8.12)
project(kaa_rsa_keygen C)

add_library(rsa_keygen kaa_rsa_key_gen.c)
target_include_directories(rsa_keygen PUBLIC ${CMAKE_CURRENT_LIST_DIR})

add_subdirectory(${KAA_SDK_DIR}/thirdparty/mbedtls kaa_mbedtls)
target_link_libraries(rsa_keygen kaa_mbedtls)
