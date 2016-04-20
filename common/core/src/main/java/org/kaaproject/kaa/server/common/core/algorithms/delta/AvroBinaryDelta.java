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

package org.kaaproject.kaa.server.common.core.algorithms.delta;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.DELTA;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class AvroBinaryDelta.
 *
 * @author Yaroslav Zeygerman
 */
public final class AvroBinaryDelta implements RawBinaryDelta {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AvroBinaryDelta.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4311409282997230034L;

    /** The schema. */
    private transient Schema schema;

    /** The delta queue. */
    private transient Queue<GenericRecord> deltaQueue;

    /** The serialized data. */
    private byte[] serializedData;

    /**
     * Instantiates a new avro binary delta during deserialization.
     */
    public AvroBinaryDelta() {
        super();
        this.deltaQueue = new LinkedList<GenericRecord>();
    }

    /**
     * Instantiates a new avro binary delta.
     *
     * @param schema
     *            the schema
     */
    public AvroBinaryDelta(Schema schema) {
        this();
        this.schema = schema;
    }

    /**
     * Adds the delta.
     *
     * @param delta
     *            the delta
     */
    public synchronized void addDelta(GenericRecord delta) {
        deltaQueue.offer(delta);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.delta.RawBinaryDelta#getData()
     */
    @Override
    public synchronized byte[] getData() throws IOException {
        if (deltaQueue != null && !deltaQueue.isEmpty()) {
            GenericArray deltaArray = new GenericData.Array(deltaQueue.size(),
                    schema);
            while (!deltaQueue.isEmpty()) {
                GenericRecord deltaT = new GenericData.Record(
                        schema.getElementType());
                deltaT.put(DELTA, deltaQueue.poll());
                deltaArray.add(deltaT);
            }
            GenericAvroConverter<GenericArray> converter = new GenericAvroConverter<>(schema);
            serializedData = converter.encode(deltaArray);
            if(LOG.isTraceEnabled()){
                LOG.trace("Delta array: {}", deltaArray.toString());
            }
        }
        return serializedData != null ? Arrays.copyOf(serializedData,
                serializedData.length) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.delta.RawBinaryDelta#hasChanges
     * ()
     */
    @Override
    public synchronized boolean hasChanges() {
        return serializedData != null || !(deltaQueue == null || deltaQueue.isEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(serializedData);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        AvroBinaryDelta other = (AvroBinaryDelta) obj;
        if (!Arrays.equals(serializedData, other.serializedData)) {
            return false;
        }
        return true;
    }

}
