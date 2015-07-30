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

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include "../../kaa_common.h"
#include "../../platform/ext_sha.h"
//#include <string.h>
#include "cc32xx_mem.h"
#include "hw_shamd5.h"
#include "hw_memmap.h"
#include "hw_types.h"
#include "hw_ints.h"
#include "hw_common_reg.h"
#include "rom.h"
#include "rom_map.h"
#include "shamd5.h"
#include "interrupt.h"
#include "prcm.h"

#include "common.h"


volatile bool g_bContextReadyFlag;

static void SHAMD5IntHandler(void)
{
    uint32_t ui32IntStatus;
    //
    // Read the SHA/MD5 masked interrupt status.
    //
    ui32IntStatus = MAP_SHAMD5IntStatus(SHAMD5_BASE, true);
    if (ui32IntStatus & SHAMD5_INT_CONTEXT_READY) {
        MAP_SHAMD5IntDisable(SHAMD5_BASE, SHAMD5_INT_CONTEXT_READY);
        g_bContextReadyFlag = true;

    }

}

static void cc32xx_init_sha()
{
    static bool cc32xx_initb = false;
    if(!cc32xx_initb) {
        MAP_PRCMPeripheralClkEnable(PRCM_DTHE, PRCM_RUN_MODE_CLK);
        MAP_SHAMD5IntRegister(SHAMD5_BASE, SHAMD5IntHandler);
        cc32xx_initb = true;
    }
}

kaa_error_t ext_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest)
{
    UART_PRINT("ext_calculate_sha_hash enter\r\n");
    KAA_RETURN_IF_NIL(digest, KAA_ERR_BADPARAM);

    int counter = 10;

    if ((data && !data_size) || (!data && data_size))
        return KAA_ERR_BADPARAM;

    UART_PRINT("ext_calculate_sha_hash 1\r\n");
    cc32xx_init_sha();

    MAP_PRCMPeripheralReset(PRCM_DTHE);
    g_bContextReadyFlag = false;
    MAP_SHAMD5IntEnable(SHAMD5_BASE, SHAMD5_INT_CONTEXT_READY |
                        SHAMD5_INT_PARTHASH_READY |
                        SHAMD5_INT_INPUT_READY |
                        SHAMD5_INT_OUTPUT_READY);

    UART_PRINT("ext_calculate_sha_hash 2\r\n");

    // Wait for the context ready flag.
    while(counter) {
        UART_PRINT("ext_calculate_sha_hash loop %d\r\n", counter);
        MAP_UtilsDelay(800000/5);
        --counter;
    }
    if(!g_bContextReadyFlag) {
        UART_PRINT("ext_calculate_sha_hash timeout\r\n");
        return KAA_ERR_TIMEOUT;
    }
    UART_PRINT("ext_calculate_sha_hash 3\r\n");
    MAP_SHAMD5ConfigSet(SHAMD5_BASE, SHAMD5_ALGO_SHA1);
    UART_PRINT("ext_calculate_sha_hash 4\r\n");
    MAP_SHAMD5DataProcess(SHAMD5_BASE, (uint8_t*)data, data_size, digest);

    UART_PRINT("ext_calculate_sha_hash exit\r\n");
    return KAA_ERR_NONE;
}

kaa_error_t ext_copy_sha_hash(kaa_digest_p dst, const kaa_digest_p src)
{
    KAA_RETURN_IF_NIL2(dst, src, KAA_ERR_BADPARAM);
    memcpy(dst, src, SHA_1_DIGEST_LENGTH);
    return KAA_ERR_NONE;
}
