set(EXTENSION_NOTIFICATION_SOURCE_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_notification_manager.c)

set(EXTENSION_NOTIFICATION_HEADER_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_notification_manager.h)

add_library(extension_notification ${EXTENSION_NOTIFICATION_SOURCE_FILES})
target_include_directories(extension_notification PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(extension_notification PUBLIC kaac)

kaa_add_unit_test(NAME test_kaa_notification_manager
    SOURCES
    ${CMAKE_CURRENT_LIST_DIR}/test/test_kaa_notification.c
    DEPENDS
    kaac ${OPENSSL_LIBRARIES})
