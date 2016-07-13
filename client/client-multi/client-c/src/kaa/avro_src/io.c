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
#include <inttypes.h>
#include "avro/io.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include "utilities/kaa_mem.h"

#include "avro_private.h"

avro_reader_t avro_reader_memory(const char *buf, int64_t len)
{
    struct avro_reader_t_ *mem_reader = (struct avro_reader_t_ *) KAA_CALLOC(1,
            sizeof(struct avro_reader_t_));
    if (!mem_reader) {
        return NULL;
    }
    mem_reader->buf = buf;
    mem_reader->len = len;
    mem_reader->read = 0;
    return mem_reader;
}

avro_writer_t avro_writer_memory(const char *buf, int64_t len)
{
    struct avro_writer_t_ *mem_writer = (struct avro_writer_t_ *) KAA_CALLOC(1,
            sizeof(struct avro_writer_t_));
    if (!mem_writer) {
        return NULL;
    }
    mem_writer->buf = buf;
    mem_writer->len = len;
    mem_writer->written = 0;
    return mem_writer;
}

static int avro_read_memory(struct avro_reader_t_ *reader, void *buf, int64_t len)
{
    if (len > 0) {
        if ((reader->len - reader->read) < len) {
            return ENOSPC;
        }
        memcpy(buf, reader->buf + reader->read, len);
        reader->read += len;
    }
    return 0;
}

#define bytes_available(reader) (reader->end - reader->cur)
#define buffer_reset(reader) {reader->cur = reader->end = reader->buffer;}

int avro_read(avro_reader_t reader, void *buf, int64_t len)
{
    if (buf && len >= 0) {
        return avro_read_memory(reader, buf, len);
    }
    return EINVAL;
}

static int avro_skip_memory(struct avro_reader_t_ *reader, int64_t len)
{
    if (len > 0) {
        if ((reader->len - reader->read) < len) {
            return ENOSPC;
        }
        reader->read += len;
    }
    return 0;
}

int avro_skip(avro_reader_t reader, int64_t len)
{
    if (len >= 0) {
        return avro_skip_memory(reader, len);
    }
    return 0;
}

static int avro_write_memory(struct avro_writer_t_ *writer, void *buf, int64_t len)
{
    if (len) {
        if ((writer->len - writer->written) < len) {
            return ENOSPC;
        }
        memcpy((void *) (writer->buf + writer->written), buf, len);
        writer->written += len;
    }
    return 0;
}

int avro_write(avro_writer_t writer, void *buf, int64_t len)
{
    if (buf && len >= 0) {
        return avro_write_memory(writer, buf, len);
    }
    return EINVAL;
}

void avro_reader_free(avro_reader_t reader)
{
    if (reader) {
        KAA_FREE(reader);
    }
}

void avro_writer_free(avro_writer_t writer)
{
    if (writer) {
        KAA_FREE(writer);
    }
}
