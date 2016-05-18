set(EXTENSION_EVENT_SOURCE_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_event.c)

set(EXTENSION_EVENT_HEADER_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_event.h)

add_library(extension_event ${EXTENSION_EVENT_SOURCE_FILES})
target_include_directories(extension_event PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(extension_event PUBLIC kaac)

kaa_add_unit_test(NAME test_event
        SOURCES
        ${CMAKE_CURRENT_LIST_DIR}/test/test_kaa_event.c
        test/kaa_test_external.c
        DEPENDS
        kaac ${OPENSSL_LIBRARIES})
