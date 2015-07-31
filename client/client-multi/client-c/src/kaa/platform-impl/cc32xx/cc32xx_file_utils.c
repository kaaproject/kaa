/*
 * Copyright 2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "cc32xx_file_utils.h"
#include <stdint.h>
#include "../../platform/stdio.h"
#include "../../utilities/kaa_mem.h"
#include "../../kaa_common.h"

#include "simplelink.h"

//Driverlib includes
#include "hw_types.h"
#include "hw_ints.h"
#include "rom.h"
#include "rom_map.h"
#include "interrupt.h"
#include "prcm.h"

//Common interface includes
#include "common.h"
#include "uart_if.h"

#define MAX_FILE_SIZE 8L*1024L

bool file_is_exist(const char *filename)
{
    unsigned long ul_token;
    long l_file_handle;
    long l_ret_val = sl_FsOpen((unsigned char *)filename, FS_MODE_OPEN_WRITE, &ul_token, &l_file_handle);

    if (l_ret_val < 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return false;
    }

    sl_FsClose(l_file_handle, 0, 0, 0);
    return true;
}

bool create_file(const char *filename)
{
    unsigned long ul_token;
    long l_file_handle;
    long l_ret_val;

    l_ret_val = sl_FsOpen((unsigned char *)filename,FS_MODE_OPEN_CREATE(MAX_FILE_SIZE, _FS_FILE_OPEN_FLAG_COMMIT|_FS_FILE_PUBLIC_WRITE|_FS_FILE_PUBLIC_READ), &ul_token, &l_file_handle);
    if (l_ret_val < 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return false;
    } else {
        l_ret_val = sl_FsClose(l_file_handle, 0, 0, 0);
        if (SL_RET_CODE_OK != l_ret_val)
            return false;
    }
    return true;
}

int cc32xx_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL4(file_name, buffer, buffer_size, needs_deallocation, -1);
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    long ret = -1;
    unsigned long ul_token;
    long l_file_handle;
    long offset = 0;
    SlFsFileInfo_t file_info;

    memset(&file_info, 0, sizeof(file_info));

    ret = sl_FsOpen((unsigned char*)file_name, FS_MODE_OPEN_READ, &ul_token, &l_file_handle);

    if (ret < 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    sl_FsGetInfo((unsigned char*)file_name, ul_token, &file_info);

    if (file_info.FileLen <= 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    unsigned char *result_buffer = (unsigned char *) KAA_MALLOC(file_info.FileLen * sizeof(unsigned char));
    if (!result_buffer) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    if ( (offset += sl_FsRead(l_file_handle, offset, result_buffer, file_info.FileLen)) == 0 ) {
        KAA_FREE(result_buffer);
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    *buffer = (char*) result_buffer;
    *buffer_size = file_info.FileLen;

    sl_FsClose(l_file_handle, 0, 0, 0);
    return 0;
}

int cc32xx_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(file_name, buffer, buffer_size, -1);

    long ret = -1;
    unsigned long ul_token;
    long l_file_handle;

    if (MAX_FILE_SIZE < buffer_size)
        return -1;

    if (!file_is_exist(file_name) && !create_file(file_name))
        return -1;

    ret = sl_FsOpen((unsigned char*)file_name, FS_MODE_OPEN_WRITE, &ul_token, &l_file_handle);
    if (ret == 0) {
        sl_FsWrite(l_file_handle, 0, (unsigned char *)buffer, buffer_size);
        sl_FsClose(l_file_handle, 0, 0, 0);
        return 0;
    }
    return -1;
}
