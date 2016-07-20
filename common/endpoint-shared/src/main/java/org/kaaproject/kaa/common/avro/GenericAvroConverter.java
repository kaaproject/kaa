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
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AvroByteArrayConverter is used to convert {#link org.apache.avro.generic.GenericContainer specific avro records} to/from bytes.
 * NOT Thread safe.
 *
 * @param <T> the generic type that extends GenericContainer
 */
public class GenericAvroConverter<T extends GenericContainer> {

    private static final Logger LOG = LoggerFactory
            .getLogger(GenericAvroConverter.class);

    private static final Charset ENCODING_CHARSET = Charset.forName("UTF-8");
    private static final Charset DECODING_CHARSET = Charset.forName("ISO-8859-1");

    private Schema schema;
    private DatumReader<T> datumReader;
    private DatumWriter<T> datumWriter;
    private BinaryDecoder binaryDecoder;
    private BinaryEncoder binaryEncoder;
    private JsonDecoder jsonDecoder;
    private JsonEncoder jsonEncoder;

    /**
     * Instantiates a new generic Avro converter.
     *
     * @param schemaSrc the schemaSrc
     */
    public GenericAvroConverter(String schemaSrc){
        this(new Schema.Parser().parse(schemaSrc));
    }

    /**
     * Instantiates a new generic avro converter.
     *
     * @param schema the schema
     */
    public GenericAvroConverter(Schema schema){
        this.schema = schema;
        datumReader = new GenericDatumReader<T>(this.schema);
        datumWriter = new GenericDatumWriter<T>(this.schema);
    }

    /**
     * Decode binary data.
     *
     * @param data the data
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeBinary(byte[] data) throws IOException{
        return decodeBinary(data, null);
    }

    /**
     * Decode binary data.
     *
     * @param data the data
     * @param reuse the reuse
     * @return the decoded object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decodeBinary(byte[] data, T reuse) throws IOException{
        binaryDecoder = DecoderFactory.get().binaryDecoder(data, binaryDecoder);
        return datumReader.read(reuse, binaryDecoder);
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
        return decodeJson(new String(data, DECODING_CHARSET), null);
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
        return datumReader.read(null, jsonDecoder);
    }

    /**
     * Encode record to Json String.
     *
     * @param record the object to encode
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String encodeToJson(T record) throws IOException{
        return new String(encodeToJsonBytes(record), ENCODING_CHARSET);
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
        datumWriter.write(record, jsonEncoder);
        jsonEncoder.flush();
        baos.flush();
        return baos.toByteArray();
    }

    /**
     * Encode record to byte array.
     *
     * @param record the object to encode
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] encode(T record) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        binaryEncoder = EncoderFactory.get().binaryEncoder(baos, binaryEncoder);
        datumWriter.write(record, binaryEncoder);
        binaryEncoder.flush();
        baos.flush();
        return baos.toByteArray();
    }

    /**
     * Convert binary data using schema to Json
     *
     * @param rawData the encoded data
     * @param dataSchema the encoded data schema
     * @return the string
     */
    public static String toJson(byte[] rawData, String dataSchema) {
        Schema schema = new Schema.Parser().parse(dataSchema);
        GenericAvroConverter<GenericContainer> converter = new GenericAvroConverter<GenericContainer>(schema);

        String json;

        try {
            GenericContainer record = converter.decodeBinary(rawData);
            json = converter.encodeToJson(record);
        } catch (IOException e) {
            LOG.warn("Can't parse json data", e);
            throw new RuntimeException(e); //NOSONAR
        }
        return json;
    }
    
    /**
     * Convert json string using schema to binary data
     *
     * @param json the json string
     * @param dataSchema the encoded data schema
     * @return the byte[]
     */
    public static byte[] toRawData(String json, String dataSchema) {
        Schema schema = new Schema.Parser().parse(dataSchema);
        GenericAvroConverter<GenericContainer> converter = new GenericAvroConverter<GenericContainer>(schema);

        byte[] rawData;

        try {
            GenericContainer record = converter.decodeJson(json);
            rawData = converter.encode(record);
        } catch (IOException e) {
            LOG.warn("Can't parse json data", e);
            throw new RuntimeException(e); //NOSONAR
        }
        return rawData;
    }

}
