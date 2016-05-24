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

#include <platform/time.h>

#include "systick.h"

#define MPU_FREQUENCY 80000000

static long long milliTimer = 1;

static void sysTickIntHandler(void)
{
    ++milliTimer;
}

void cc32xx_init_timer(void)
{
    static int init = 0;

    if (!init) {
        SysTickEnable();
        SysTickIntEnable();
        SysTickIntRegister(sysTickIntHandler);
        SysTickPeriodSet(MPU_FREQUENCY / 1000);/* 1 ms */
        init = 1;
    }
}

long long cc32xx_clock_getms(void)
{
    cc32xx_init_timer();
    return milliTimer;
}

long cc32xx_time(void)
{
    cc32xx_init_timer();
    return milliTimer / 1000;
}
