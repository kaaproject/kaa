cmake_minimum_required(VERSION 2.8.12)
project(mbedtls C)

set(KAA_MBEDTLS_SOURCE_FILES
        ${CMAKE_CURRENT_LIST_DIR}/aes.c
        ${CMAKE_CURRENT_LIST_DIR}/asn1parse.c
        ${CMAKE_CURRENT_LIST_DIR}/asn1write.c
        ${CMAKE_CURRENT_LIST_DIR}/bignum.c
        ${CMAKE_CURRENT_LIST_DIR}/ctr_drbg.c
        ${CMAKE_CURRENT_LIST_DIR}/entropy.c
        ${CMAKE_CURRENT_LIST_DIR}/entropy_poll.c
        ${CMAKE_CURRENT_LIST_DIR}/md.c
        ${CMAKE_CURRENT_LIST_DIR}/md_wrap.c
        ${CMAKE_CURRENT_LIST_DIR}/oid.c
        ${CMAKE_CURRENT_LIST_DIR}/pk.c
        ${CMAKE_CURRENT_LIST_DIR}/pkwrite.c
        ${CMAKE_CURRENT_LIST_DIR}/pem.c
        ${CMAKE_CURRENT_LIST_DIR}/pk_wrap.c
        ${CMAKE_CURRENT_LIST_DIR}/rsa.c
        ${CMAKE_CURRENT_LIST_DIR}/sha256.c
        ${CMAKE_CURRENT_LIST_DIR}/pkparse.c
        ${CMAKE_CURRENT_LIST_DIR}/sha1.c
        ${CMAKE_CURRENT_LIST_DIR}/base64.c
)


add_library(mbedtls ${KAA_MBEDTLS_SOURCE_FILES})
target_include_directories(mbedtls PUBLIC ${CMAKE_CURRENT_LIST_DIR}/mbedtls ${CMAKE_CURRENT_LIST_DIR})
