set(EXTENSION_CONFIGURATION_SOURCE_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_configuration_manager.c)

set(EXTENSION_CONFIGURATION_HEADER_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_configuration_manager.h)

add_library(extension_configuration ${EXTENSION_CONFIGURATION_SOURCE_FILES})
target_include_directories(extension_configuration PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(extension_configuration PUBLIC kaac)

kaa_add_unit_test(NAME test_kaa_configuration_manager
    SOURCES
    ${CMAKE_CURRENT_LIST_DIR}/test/test_kaa_configuration.c
    test/kaa_test_external.c
    DEPENDS
    kaac extension_configuration ${OPENSSL_LIBRARIES})
