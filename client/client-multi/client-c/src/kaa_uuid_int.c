/*
 * Copyright 2014 CyberVision, Inc.
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

#include "kaa_uuid.h"
#include "kaa_mem.h"
#include <stdio.h>
#include <string.h>

#define KAA_UUID_FORMAT     "%u"

void kaa_uuid_fill(kaa_uuid_t *dst, KAA_UUID_VALUE_T src)
{
    *dst = src;
}

void kaa_uuid_copy(kaa_uuid_t *dst, kaa_uuid_t *src)
{
    *dst = *src;
}

void kaa_uuid_to_string(char **dst, kaa_uuid_t *uuid)
{
    char buf[11];
    size_t len = sprintf(buf, KAA_UUID_FORMAT, *uuid);
    *dst = (char *) KAA_MALLOC((len + 1) * sizeof(char));
    strcpy(*dst, buf);
}

void kaa_uuid_from_string(const char *src, kaa_uuid_t *uuid)
{
    sscanf(src, KAA_UUID_FORMAT, uuid);
}

int kaa_uuid_compare(kaa_uuid_t *uuid1, kaa_uuid_t *uuid2)
{
    return *uuid1 - *uuid2;
}
