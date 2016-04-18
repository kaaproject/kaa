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

package org.kaaproject.kaa.server.common.dao.impl;

import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * The Class DaoUtil.
 */
public abstract class DaoUtil {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DaoUtil.class);

    private DaoUtil() {
    }

    /**
     * This method take string id from <code>GenericModel</code> object
     *
     * @param model <code>GenericModel</code> object
     * @return the id as string type
     */
    public static String idToString(GenericModel<? extends Object> model) {
        String id = null;
        if (model != null) {
            Long lid = model.getId();
            id = lid != null ? lid.toString() : null;
        }
        return id;
    }

    /**
     * This method convert Long id to String object.
     *
     * @param lid basic mongoDB id object type.
     * @return converted to string id.
     */
    public static String idToString(Long lid) {
        String id = null;
        if (lid != null) {
            id = lid.toString();
        }
        return id;
    }

    /**
     * This method  convert list of model objects to dto objects.
     *
     * @param <T>       Type of model object
     * @param toDtoList List of model objects.
     * @return List of converted objects.
     */
    public static <T> List<T> convertDtoList(Collection<? extends ToDto<T>> toDtoList) {
        List<T> list = Collections.emptyList();
        if (toDtoList != null && !toDtoList.isEmpty()) {
            list = new ArrayList<>();
            for (ToDto<T> object : toDtoList) {
                list.add(object.toDto());
            }
        }
        return list;
    }

    /**
     * This method  convert list of model objects to dto objects.
     *
     * @param <T>       Type of model object
     * @param toDtoSet List of model objects.
     * @return List of converted objects.
     */
    public static <T> Set<T> convertDtoSet(Collection<? extends ToDto<T>> toDtoSet) {
        Set<T> set = Collections.emptySet();
        if (toDtoSet != null && !toDtoSet.isEmpty()) {
            set = new HashSet<>();
            for (ToDto<T> object : toDtoSet) {
                set.add(object.toDto());
            }
        }
        return set;
    }

    /**
     * This method  convert model object to dto object.
     *
     * @param <T> Type of model object
     * @param dto Model object
     * @return converted object
     */
    public static <T> T getDto(ToDto<T> dto) {
        T object = null;
        if (dto != null) {
            object = dto.toDto();
        }
        return object;
    }

    /**
     * This method use for coping array bytes.
     *
     * @param array bytes
     * @return copied array of bytes
     */
    public static byte[] getArrayCopy(byte[] array) {
        byte[] bytes = null;
        if (array != null) {
            bytes = Arrays.copyOf(array, array.length);
        }
        return bytes;
    }

    /**
     * This method find file by name in context and convert input of file to string.
     *
     * @param name of the file
     * @param clazz the clazz
     * @return String
     */
    public static String getStringFromFile(String name, Class<?> clazz) {
        String data = "";
        if (isNotBlank(name)) {
            InputStream is = clazz.getClass().getResourceAsStream(name);
            if (is != null) {
                LOG.trace("Load input stream of file {}", is);
                try {
                    byte[] arrayData = IOUtils.toByteArray(is);
                    if (arrayData != null) {
                        data = new String(arrayData, Charset.forName("UTF-8"));
                    }
                } catch (IOException e) {
                    LOG.error("Can't read data from file", e);
                }
            }
        }
        return data;
    }
}
