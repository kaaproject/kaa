#include <stddef.h>
#include <stdbool.h>
#include <platform/ext_configuration_persistence.h>

#include "esp8266_file_utils.h"

#define KAA_CONFIGURATION_STORAGE "" /* WTF?? no file system! */

void ext_configuration_read(char **buffer, size_t *buffer_size, bool *needs_deallocation) {

}

void ext_configuration_store(const char *buffer, size_t buffer_size) {

}

