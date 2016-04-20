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

package org.kaaproject.kaa.common.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * The Class AvroByteArrayConverter is used to convert {#link org.apache.avro.specific.SpecificRecordBase specific Avro records} to/from bytes.
 * NOT Thread safe.
 *
 * @param <T> the generic type that extends SpecificRecordBase
 *
 * @author Andrew Shvayka
 */
public class AvroByteArrayConverter<T extends SpecificRecordBase> {
    SpecificDatumReader<T> avroReader;
    SpecificDatumWriter<T> avroWriter;
    BinaryEncoder encoder;
    BinaryDecoder decoder;

    /**
     * Instantiates a new Avro byte array converter based on class.
     *
     * @param typeParameterClass the type parameter class
     */
    public AvroByteArrayConverter(Class<T> typeParameterClass) {
        super();
        avroReader = new SpecificDatumReader<T>(typeParameterClass);
        avroWriter = new SpecificDatumWriter<T>(typeParameterClass);
    }

    /**
     * Converts object to byte array
     *
     * @param avroObject the avro object
     * @return the byte[] result of conversion
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] toByteArray(T avroObject) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().binaryEncoder(baos, encoder);
        avroWriter.write(avroObject, encoder);
        encoder.flush();
        return baos.toByteArray();
    }

    /**
     * Creates object from byte array
     *
     * @param data the data
     * @return the result of conversion
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T fromByteArray(byte[] data) throws IOException {
        return fromByteArray(data, null);
    }

    /**
     * Creates object from byte array
     *
     * @param data the data
     * @param reuse object to reuse
     * @return the result of conversion
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T fromByteArray(byte[] data, T reuse) throws IOException {
        decoder = DecoderFactory.get().binaryDecoder(data, decoder);
        return avroReader.read(reuse, decoder);
    }
}
