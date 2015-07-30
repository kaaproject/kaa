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

#define MAX_FILE_SIZE        8L*1024L

char config_file[2*1024];
char status_file[512];
int config_file_size = 0;
int status_file_size = 0;

void init_fs_device() {
    static int init_dev = 0;

    if(!init_dev) {
        //sl_Start(0, 0, 0);
        init_dev = 1;

        memset(config_file, 0, sizeof(config_file));
        memset(status_file, 0, sizeof(status_file));
    }
}

bool file_is_exist(const char *filename) {

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

bool create_file(const char *filename) {

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
    init_fs_device();

    UART_PRINT("cc32xx_binary_file_read enter %s\r\n", file_name);
    KAA_RETURN_IF_NIL4(file_name, buffer, buffer_size, needs_deallocation, -1);
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    if(strcmp(file_name, "kaa_configuration.bin") == 0 && config_file_size)
    {
        char *tmp = (char*) KAA_MALLOC(config_file_size * sizeof(char));
        memcpy(tmp, config_file, config_file_size);
        *buffer = tmp;
        *buffer_size = config_file_size;
        return 0;
    }
    else if(strcmp(file_name, "kaa_status.bin") == 0)
    {
        char *tmp = (char*) KAA_MALLOC(status_file_size * sizeof(char));
        memcpy(tmp, status_file, status_file_size);
        *buffer = tmp;
        *buffer_size = status_file_size;
        return 0;
    }
    return -1;

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

//    UART_PRINT("cc32xx_binary_file_read file info alloc %d size %d\r\n", file_info.AllocatedLen, file_info.FileLen);

    if (file_info.FileLen <= 0) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

    unsigned char *result_buffer = (unsigned char *) KAA_MALLOC(file_info.FileLen * sizeof(unsigned char));
    if (!result_buffer) {
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

//    UART_PRINT("cc32xx_binary_file_read read\r\n");
    if ( (offset += sl_FsRead(l_file_handle, offset, result_buffer, file_info.FileLen)) == 0 ) {
//        UART_PRINT("cc32xx_binary_file_read read fail\r\n");
        KAA_FREE(result_buffer);
        sl_FsClose(l_file_handle, 0, 0, 0);
        return -1;
    }

//    UART_PRINT("cc32xx_binary_file_read readed %d\r\n", offset);
//    for(int i = 0; i < offset; ++i)
//    {
//        if(!(i % 20))
//            UART_PRINT("\r\n");
//        UART_PRINT("%02X ", result_buffer[i]);
//    }
//    UART_PRINT("\r\n");

    *buffer = (char*) result_buffer;
    *buffer_size = file_info.FileLen;

    sl_FsClose(l_file_handle, 0, 0, 0);
    return 0;
}

int cc32xx_binary_file_store(const char *file_name, const char *buffer, size_t buffer_size)
{
    init_fs_device();
    KAA_RETURN_IF_NIL3(file_name, buffer, buffer_size, -1);
    UART_PRINT("cc32xx_binary_file_store file_name %s\r\n", file_name);

    if(strcmp(file_name, "kaa_configuration.bin") == 0 && config_file_size)
    {
        memcpy(config_file, buffer, buffer_size);
        config_file_size = buffer_size;
        return 0;
    }
    else if(strcmp(file_name, "kaa_status.bin") == 0)
    {
        memcpy(status_file, buffer, buffer_size);
        status_file_size = buffer_size;
        return 0;
    }
    return -1;

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
