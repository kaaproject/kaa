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

package org.kaaproject.kaa.server.common.log.shared;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * Utility class that allow usage of Avro records in Apache Spark and other frameworks that require support of Java serialization. 
 * 
 * @author Andrew Shvayka
 *
 * @param <T> avro generated record
 */
public class AvroSerializationWrapper<T extends SpecificRecordBase> implements Externalizable {

    private static final ThreadLocal<Map<String, AvroReader<? extends SpecificRecordBase>>> recordReaderMap = //NOSONAR
            new ThreadLocal<Map<String, AvroReader<? extends SpecificRecordBase>>>() {
        protected Map<String, AvroReader<? extends SpecificRecordBase>> initialValue() {
            return new HashMap<String, AvroReader<? extends SpecificRecordBase>>();
        }
    };

    private static final ThreadLocal<Map<String, AvroWriter<? extends SpecificRecordBase>>> recordWriterMap = //NOSONAR
            new ThreadLocal<Map<String, AvroWriter<? extends SpecificRecordBase>>>() {
        protected Map<String, AvroWriter<? extends SpecificRecordBase>> initialValue() {
            return new HashMap<String, AvroWriter<? extends SpecificRecordBase>>();
        }
    };

    private final Class<T> clazz;
    private final String className;
    private T avroObject;

    public AvroSerializationWrapper(Class<T> clazz) {
        this(clazz, null);
    }

    public AvroSerializationWrapper(Class<T> clazz, T avroObject) {
        this.clazz = clazz;
        this.className = clazz.getName();
        this.avroObject = avroObject;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        @SuppressWarnings("unchecked")
        AvroWriter<T> writer = (AvroWriter<T>) recordWriterMap.get().get(className);
        if (writer == null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writer = new AvroWriter<T>(new SpecificDatumWriter<T>(clazz), EncoderFactory.get().binaryEncoder(os, null));
            recordWriterMap.get().put(className, writer);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(os, writer.getEncoder());
        writer.getWriter().write(avroObject, encoder);

        out.write(os.size());
        out.write(os.toByteArray());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        byte[] data = new byte[size];
        in.read(data);
        @SuppressWarnings("unchecked")
        AvroReader<T> reader = (AvroReader<T>) recordReaderMap.get().get(className);
        if (reader == null) {
            reader = new AvroReader<T>(new SpecificDatumReader<T>(clazz), DecoderFactory.get().binaryDecoder(data, null));
            recordReaderMap.get().put(className, reader);
        }
        BinaryDecoder recordDataDecoder = DecoderFactory.get().binaryDecoder(data, reader.getDecoder());
        avroObject = reader.getReader().read(null, recordDataDecoder);
    }

    public T get() {
        return avroObject;
    }

    public void set(T avroObject) {
        this.avroObject = avroObject;
    }

    private static class AvroReader<T extends SpecificRecordBase> {
        private final SpecificDatumReader<T> reader;
        private final BinaryDecoder decoder;

        public AvroReader(SpecificDatumReader<T> reader, BinaryDecoder decoder) {
            super();
            this.reader = reader;
            this.decoder = decoder;
        }

        public SpecificDatumReader<T> getReader() {
            return reader;
        }

        public BinaryDecoder getDecoder() {
            return decoder;
        }
    }

    private static class AvroWriter<T extends SpecificRecordBase> {
        private final SpecificDatumWriter<T> writer;
        private final BinaryEncoder encoder;

        public AvroWriter(SpecificDatumWriter<T> writer, BinaryEncoder encoder) {
            super();
            this.writer = writer;
            this.encoder = encoder;
        }

        public SpecificDatumWriter<T> getWriter() {
            return writer;
        }

        public BinaryEncoder getEncoder() {
            return encoder;
        }
    }
}