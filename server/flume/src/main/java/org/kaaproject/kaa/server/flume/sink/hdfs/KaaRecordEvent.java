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

package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.util.Map;

import org.apache.flume.Event;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

public class KaaRecordEvent implements Event {

    private RecordHeader recordHeader;
    private Map<String, String> headers;
    private byte[] body;
    
    public KaaRecordEvent(RecordHeader recordHeader, Map<String, String> headers, byte[] body) {
        this.recordHeader = recordHeader;
        this.headers = headers;
        this.body = body;
    }

    public RecordHeader getRecordHeader() {
        return recordHeader;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public void setBody(byte[] body) {
        this.body = body;
    }
    
}
