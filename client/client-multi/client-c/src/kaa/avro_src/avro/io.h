/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

#ifndef AVRO_IO_H
#define AVRO_IO_H
#ifdef __cplusplus
extern "C" {
#endif

#include "platform.h"
#include <stdint.h>

struct avro_reader_t_ {
    const char *buf;
    int64_t len;
    int64_t read;
};

struct avro_writer_t_ {
    const char *buf;
    int64_t len;
    int64_t written;
};

typedef struct avro_reader_t_ *avro_reader_t;
typedef struct avro_writer_t_ *avro_writer_t;

avro_reader_t avro_reader_memory(const char *buf, int64_t len);
avro_writer_t avro_writer_memory(const char *buf, int64_t len);

int avro_read(avro_reader_t reader, void *buf, int64_t len);
int avro_skip(avro_reader_t reader, int64_t len);
int avro_write(avro_writer_t writer, void *buf, int64_t len);

void avro_reader_free(avro_reader_t reader);
void avro_writer_free(avro_writer_t writer);

#ifdef __cplusplus
} // extern "C"
#endif
#endif
