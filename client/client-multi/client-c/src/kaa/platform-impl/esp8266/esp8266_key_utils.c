#include <stdbool.h>
#include <stddef.h>
#include <platform/ext_key_utils.h>
#include "rsa.h"


void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, 
                                            bool *needs_deallocation) {
    *buffer = (char*)KAA_PUBLIC_KEY_DATA;
    *buffer_size = KAA_PUBLIC_KEY_LENGTH;
    *needs_deallocation = false;
}
