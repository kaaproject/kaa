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

package org.kaaproject.kaa.common.hash;

import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * The class EndpointObjectHash is responsible for hash calculation
 *
 * @author Andrew Shvayka
 */
public final class EndpointObjectHash implements Serializable {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final long serialVersionUID = 1L;

    private final byte[] data;

    /**
     * Instantiates a new endpoint object hash.
     *
     * @param data the data
     */
    private EndpointObjectHash(byte[] data) {
        super();
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * From bytes.
     *
     * @param data the data
     * @return the endpoint object hash
     */
    public static EndpointObjectHash fromString(String data) {
        if (data == null) {
            return null;
        }
        return new EndpointObjectHash(Base64.encodeBase64(data.getBytes(UTF8)));
    }

    /**
     * From bytes.
     *
     * @param data the data
     * @return the endpoint object hash
     */
    public static EndpointObjectHash fromBytes(byte[] data) {
        if (data == null) {
            return null;
        }
        return new EndpointObjectHash(data);
    }

    /**
     * creates EndpointObjectHash using SHA1 algorithm over String representation of an object.
     *
     * @param data the data
     * @return the endpoint object hash
     */
    public static EndpointObjectHash fromSHA1(String data) {
        if (data == null) {
            return null;
        }
        return new EndpointObjectHash(SHA1HashUtils.hashToBytes(data));
    }

    /**
     * creates EndpointObjectHash using SHA1 algorithm over binary representation of an object.
     *
     * @param data the data
     * @return the endpoint object hash
     */
    public static EndpointObjectHash fromSHA1(byte[] data) {
        if (data == null) {
            return null;
        }
        return new EndpointObjectHash(SHA1HashUtils.hashToBytes(data));
    }

    /**
     * Gets the data of a hash.
     *
     * @return the data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
    
    /**
     * Gets the byte buffer with a hash.
     *
     * @return the data
     */
    public ByteBuffer getDataBuf() {
        return ByteBuffer.wrap(getData());
    }

    /**
     * Checks if objects are binary equal.
     *
     * @param data the data
     * @return true, if successful
     */
    public boolean binaryEquals(byte[] data) {
        return Arrays.equals(this.data, data);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(data);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    /* (non-Javadoc)
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
        EndpointObjectHash other = (EndpointObjectHash) obj;
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        return true;
    }
}
