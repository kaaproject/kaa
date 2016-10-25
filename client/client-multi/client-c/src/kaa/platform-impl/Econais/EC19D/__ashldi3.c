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

#include <inttypes.h>

uint64_t __ashldi3(uint64_t a, int b)
{
    if (b <= 0) {
        return a;
    }
    if (b >= 64) {
        return 0;
    }
    uint32_t aLow = (uint32_t)a;
    uint32_t aHigh = (uint32_t)(a >> 32);
    if (b >= 32) {
        aHigh = (aLow << (b - 32));
        aLow = 0;
    } else {
        aHigh = (aHigh << b) + (aLow >> (32 - b));
        aLow = (aLow << b);
    }
    return ((uint64_t) aHigh << 32) + aLow;
}
