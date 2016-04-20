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

package org.kaaproject.kaa.common.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DtoByteMarshaller {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DtoByteMarshaller.class);

    private DtoByteMarshaller() {
    }

    public static <T> byte[] toBytes(T object) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(byteStream);
            out.writeObject(object);
        } catch (IOException e) {
            LOG.error("Can't convert object to bytes.", e);
        }
        return byteStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        T object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            object = (T) objectInputStream.readObject();
        } catch (Exception e) {
            LOG.error("Can't convert bytes to object.", e);
        }
        return object;
    }

}
