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
import java.nio.charset.Charset;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * The Class AvroJsonConverter is used to convert {#link org.apache.avro.specific.SpecificRecordBase specific Avro records} to/from json.
 * NOT Thread safe.
 *
 * @param <T> the generic type that extends SpecificRecordBase
 *
 * @author Igor Kulikov
 */
public class AvroJsonConverter<T extends SpecificRecordBase> {
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    Schema schema;
    SpecificDatumReader<T> avroReader;
    SpecificDatumWriter<T> avroWriter;
    JsonDecoder jsonDecoder;
    JsonEncoder jsonEncoder;

    /**
     * Instantiates a new Avro json converter based on class.
     *
     * @param schema                the schema
     * @param typeParameterClass    the type parameter class
     */
    public AvroJsonConverter(Schema schema, Class<T> typeParameterClass) {
        super();
        this.schema = schema;
        avroReader = new SpecificDatumReader<T>(typeParameterClass);
        avroWriter = new SpecificDatumWriter<T>(typeParameterClass);
    }
    
    /**
     * Decode json data.
     *
     * @param data the data
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(String data) throws IOException{
        return decodeJson(data, null);
    }

    /**
     * Decode json data.
     *
     * @param data the data
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(byte[] data) throws IOException {
        return decodeJson(new String(data, UTF8), null);
    }

    /**
     * Decode json data.
     *
     * @param data the data
     * @param reuse the reuse
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeJson(String data, T reuse) throws IOException{
        jsonDecoder = DecoderFactory.get().jsonDecoder(this.schema, data, true);
        return avroReader.read(null, jsonDecoder);
    }
    
    /**
     * Encode record to Json String.
     *
     * @param record the object to encode
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String endcodeToJson(T record) throws IOException{
        return new String(encodeToJsonBytes(record), UTF8);
    }

    /**
     * Encode record to Json and then convert to byte array.
     *
     * @param record the object to encode
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] encodeToJsonBytes(T record) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jsonEncoder = EncoderFactory.get().jsonEncoder(this.schema, baos, true);
        avroWriter.write(record, jsonEncoder);
        jsonEncoder.flush();
        baos.flush();
        return baos.toByteArray();
    }

}
