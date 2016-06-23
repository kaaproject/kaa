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

#include <stdio.h>
#include <stdlib.h>
#include <kaa/kaa.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_error.h>


static void dummy_function(void *context)
{
    printf("Hello, I am a Kaa Application!\n");
    kaa_client_stop(context);
}

int main(void)
{
    kaa_client_t *kaa_client = NULL;
    kaa_error_t error = kaa_client_create(&kaa_client, NULL);
    if (error) {
      return EXIT_FAILURE;
    }

    error = kaa_client_start(kaa_client, dummy_function, (void *)kaa_client, 0);

    kaa_client_destroy(kaa_client);

    if (error) {
      return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
