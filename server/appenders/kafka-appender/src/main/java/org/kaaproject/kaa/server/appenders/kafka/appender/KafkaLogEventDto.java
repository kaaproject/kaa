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

package org.kaaproject.kaa.server.appenders.kafka.appender;

import java.io.Serializable;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

public class KafkaLogEventDto implements Serializable {

    private static final long serialVersionUID = 5708819593518595947L;

    private final RecordHeader header;
    private final GenericRecord event;

    public KafkaLogEventDto(RecordHeader header, GenericRecord event) {
        super();
        this.header = header;
        this.event = event;
    }

    public RecordHeader getHeader() {
        return header;
    }

    public GenericRecord getEvent() {
        return event;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((header == null) ? 0 : header.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KafkaLogEventDto other = (KafkaLogEventDto) obj;
        if (event == null) {
            if (other.event != null) {
                return false;
            }
        } else if (!event.equals(other.event)) {
            return false;
        }
        if (header == null) {
            if (other.header != null) {
                return false;
            }
        } else if (!header.equals(other.header)) {
            return false;
        }
        return true;
    }
}
