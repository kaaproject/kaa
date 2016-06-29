#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

cmake_minimum_required(VERSION 3.2.2)
project(kaa_rsa_keygen C)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -std=c99 -Wall -Wextra -pedantic -D_GNU_SOURCE")

set(MBEDTLS_DIRECTORY "${CMAKE_CURRENT_LIST_DIR}/../../thirdparty/mbedtls")

set(KAA_RSA_KEYGEN_SOURCE_FILES
        ${MBEDTLS_DIRECTORY}/aes.c
        ${MBEDTLS_DIRECTORY}/asn1parse.c
        ${MBEDTLS_DIRECTORY}/asn1write.c
        ${MBEDTLS_DIRECTORY}/bignum.c
        ${MBEDTLS_DIRECTORY}/ctr_drbg.c
        ${MBEDTLS_DIRECTORY}/entropy.c
        ${MBEDTLS_DIRECTORY}/entropy_poll.c
        ${MBEDTLS_DIRECTORY}/md.c
        ${MBEDTLS_DIRECTORY}/md_wrap.c
        ${MBEDTLS_DIRECTORY}/oid.c
        ${MBEDTLS_DIRECTORY}/pk.c
        ${MBEDTLS_DIRECTORY}/pkwrite.c
        ${MBEDTLS_DIRECTORY}/pk_wrap.c
        ${MBEDTLS_DIRECTORY}/rsa.c
        ${MBEDTLS_DIRECTORY}/sha256.c
        ${MBEDTLS_DIRECTORY}/pkparse.c
        ${MBEDTLS_DIRECTORY}/sha1.c
        ${MBEDTLS_DIRECTORY}/base64.c
        ${CMAKE_CURRENT_LIST_DIR}/kaa_rsa_key_gen.c
)

add_executable("generate_rsa_keys" ${KAA_RSA_KEYGEN_SOURCE_FILES})
target_include_directories("generate_rsa_keys" PUBLIC ${MBEDTLS_DIRECTORY})
