if(KAA_MAX_LOG_LEVEL EQUAL 7)
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_TRACE_MEMORY_ALLOCATIONS")
endif()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DESP8266_PLATFORM")

set(ESP8266_SRC_FOLDER ${KAA_SRC_FOLDER}/platform-impl/esp8266)

set(ESP8266_SRC
    ${ESP8266_SRC_FOLDER}/esp8266_logger.c
    ${ESP8266_SRC_FOLDER}/esp8266_kaa_client.c
    ${ESP8266_SRC_FOLDER}/esp8266_configuration_persistence.c
    ${ESP8266_SRC_FOLDER}/esp8266_status.c
    ${ESP8266_SRC_FOLDER}/esp8266_key_utils.c
    ${ESP8266_SRC_FOLDER}/esp8266_tcp_utils.c
    ${ESP8266_SRC_FOLDER}/esp8266_kaa_failover_strategy.c
    ${ESP8266_SRC_FOLDER}/exit.c
    ${ESP8266_SRC_FOLDER}/snprintf.c
    #    ${ESP8266_SRC_FOLDER}/gmtime.c
    ${KAA_SRC_FOLDER}/platform-impl/sha.c
    ${KAA_SRC_FOLDER}/platform-impl/sha1.c
    ${KAA_SRC_FOLDER}/platform-impl/ext_log_storage_memory.c
    ${KAA_SRC_FOLDER}/platform-impl/ext_log_upload_strategy_by_volume.c
    ${KAA_SRC_FOLDER}/platform-impl/kaa_tcp_channel.c
    ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_parser.c
    ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_request.c
    )

set(KAA_SOURCE_FILES ${KAA_SOURCE_FILES} ${ESP8266_SRC})

set(KAA_INCLUDE_PATHS ${KAA_INCLUDE_PATHS} ${ESP8266_INCDIRS})

add_custom_target(rsa ALL DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/src/kaa/platform-impl/esp8266/rsa.h)

add_custom_command(TARGET rsa
                   PRE_BUILD
                   COMMAND java -jar ${CMAKE_CURRENT_SOURCE_DIR}/tools/pub_key_generator.jar
                   ${CMAKE_CURRENT_SOURCE_DIR}/src/kaa/platform-impl/esp8266/rsa.h)
