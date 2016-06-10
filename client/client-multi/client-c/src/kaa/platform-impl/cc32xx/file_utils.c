/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#include <platform/file_utils.h>
#include <stdint.h>
#include <platform/stdio.h>
#include "utilities/kaa_mem.h"
#include "kaa_common.h"

#include <platform/sock.h>

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

// TODO: KAA-845
// There is no file consistency check when parsing persistence data. This causes
// bugs when data inside a file is misinterpreted.
#if 0
static bool file_is_exist(const char *filename)
{
    uint32_t ul_token;
    int32_t l_file_handle;
    int32_t l_ret_val = sl_FsOpen((unsigned char *)filename, FS_MODE_OPEN_WRITE, &ul_token, &l_file_handle);

    if (l_ret_val < 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return false;
    }

    sl_FsClose(l_file_handle, 0, 0, 0);
    return true;
}

static bool create_file(const char *filename)
{
    uint32_t ul_token;
    int32_t  l_file_handle;
    int32_t  l_ret_val;

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
#endif

int cc32xx_binary_file_read(const char *file_name, char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
// TODO: KAA-845
// There is no file consistency check when parsing persistence data. This causes
// bugs when data inside a file is misinterpreted.
#if 0
    KAA_RETURN_IF_NIL4(file_name, buffer, buffer_size, needs_deallocation, -1);
    *buffer = NULL;
    *buffer_size = 0;

    int32_t ret = -1;
    uint32_t ul_token;
    int32_t l_file_handle;
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

    uint8_t *result_buffer = (uint8_t*) KAA_MALLOC(file_info.FileLen * sizeof(uint8_t));
    if (!result_buffer) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    if ( sl_FsRead(l_file_handle, 0, result_buffer, file_info.FileLen) == 0 ) {
        KAA_FREE(result_buffer);
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    *buffer = (char*) result_buffer;
    *buffer_size = file_info.FileLen;
    *needs_deallocation = true;

    sl_FsClose(l_file_handle, 0, 0, 0);
    return 0;
#else
    (void)file_name;
    (void)buffer;
    (void)buffer_size;
    (void)needs_deallocation;
#endif
    return -1;
}

int cc32xx_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size)
{
// TODO: KAA-845
// There is no file consistency check when parsing persistence data. This causes
// bugs when data inside a file is misinterpreted.
#if 0
    KAA_RETURN_IF_NIL3(file_name, buffer, buffer_size, -1);

    int32_t ret = -1;
    uint32_t ul_token;
    int32_t l_file_handle;

    if (MAX_FILE_SIZE < buffer_size)
        return ret;

    if (!file_is_exist(file_name) && !create_file(file_name))
        return ret;

    ret = sl_FsOpen((unsigned char*)file_name, FS_MODE_OPEN_WRITE, &ul_token, &l_file_handle);
    if (ret == 0) {
        if(sl_FsWrite(l_file_handle, 0, (unsigned char *)buffer, buffer_size))
            ret = 0;
        sl_FsClose(l_file_handle, 0, 0, 0);
    }
    return ret;
#else
    (void)file_name;
    (void)buffer;
    (void)buffer_size;
    return -1;
#endif
}

int cc32xx_binary_file_delete(const char *file_name)
{
// TODO: KAA-845
// There is no file consistency check when parsing persistence data. This causes
// bugs when data inside a file is misinterpreted.
#if 0
    int32_t ret = 0;
    uint32_t ul_token;
    int32_t l_file_handle;

    ret = sl_FsOpen((unsigned char*)file_name, FS_MODE_OPEN_READ, &ul_token, &l_file_handle);
    sl_FsClose(l_file_handle, 0, 0, 0);
    if (ret < 0)
        return -1;

    ret = sl_FsDel(file_name, 0);
    if (ret < 0)
        return -1;

    return 0;
#else
    (void)file_name;
    return -1;
#endif
}
