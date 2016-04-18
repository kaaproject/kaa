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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ModelUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ModelUtils.class);
    public static final String UTF8 = "UTF-8";

    private ModelUtils() {
        throw new UnsupportedOperationException("Not supported");
    }

    public static String getStringId(Long longId) {
        return longId != null ? longId.toString() : null;
    }

    public static String getStringId(GenericModel<?> hasId) {
        String stringId = null;
        if (hasId != null) {
            Long id = hasId.getId();
            stringId = getStringId(id);
        }
        return stringId;
    }

    public static Long getLongId(HasId hasId) {
        Long longId = null;
        if (hasId != null) {
            String id = hasId.getId();
            longId = getLongId(id);
        }
        return longId;
    }

    public static Long getLongId(String id) {
        Long longId = null;
        if (isNotBlank(id)) {
            try {
                longId = Long.valueOf(id);
            } catch (NumberFormatException e) {
                LOG.error("Can't convert to Long id. Incorrect String id {} ", id);
            }
        }
        return longId;
    }

    public static Set<Topic> getTopic(List<String> topicIds) {
        Set<Topic> topics = Collections.emptySet();
        if (topicIds != null && !topicIds.isEmpty()) {
            topics = new HashSet<>();
            for (String id : topicIds) {
                Long lid = getLongId(id);
                if (lid != null) {
                    topics.add(new Topic(lid));
                }
            }
        }
        return topics;
    }

    public static <V> Set<V> getGenericModelIds(Set<String> idSet, GenericModel model) {
        Set<V> dataSet = new HashSet<>();
        for (String id : idSet) {
            dataSet.add((V) model.newInstance(id));
        }
        return dataSet;
    }

    public static List<String> getDtoIds(Set<HasId> set) {
        List<String> ids;
        if (set == null || set.isEmpty()) {
            ids = Collections.emptyList();
        } else {
            ids = new ArrayList<>(set.size());
            for (HasId hasId : set) {
                ids.add(hasId.getId());
            }
        }
        return ids;
    }

    public static Set<String> getGenericModelIds(Set<? extends GenericModel<?>> set) {
        Set<String> ids;
        if (set == null || set.isEmpty()) {
            ids = Collections.emptySet();
        } else {
            ids = new HashSet<>(set.size());
            for (GenericModel<?> model : set) {
                ids.add(model.getStringId());
            }
        }
        return ids;
    }

    public static Set<Integer> getGenericModelVersions(Set<? extends HasVersion> set) {
        Set<Integer> ids;
        if (set == null || set.isEmpty()) {
            ids = Collections.emptySet();
        } else {
            ids = new HashSet<>(set.size());
            for (HasVersion  hasVersion: set) {
                ids.add(hasVersion.getVersion());
            }
        }
        return ids;
    }

    public static List<String> getTopicIds(Set<Topic> topics) {
        List<String> ids = Collections.emptyList();
        if (topics != null && !topics.isEmpty()) {
            ids = new ArrayList<>();
            for (Topic topic : topics) {
                String id = getStringId(topic.getId());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    public static List<TopicDto> getTopicDtos(List<String> topicIds) {
        List<TopicDto> topics = Collections.emptyList();
        if (topicIds != null && !topicIds.isEmpty()) {
            topics = new ArrayList<>(topicIds.size());
            for (String id : topicIds) {
                if (id != null) {
                    TopicDto topicDto = new TopicDto();
                    topicDto.setId(id);
                    topics.add(topicDto);
                }
            }
        }
        return topics;
    }

    public static String binaryToString(byte[] data) {
        String body = null;
        if (data != null) {
            try {
                body = new String(data, UTF8);
            } catch (UnsupportedEncodingException e) {
                LOG.warn("Can't convert binary data to string. ", e);
            }
        }
        return body;
    }

    public static byte[] stringToBinary(String body) {
        byte[] data = null;
        if (body != null) {
            try {
                data = body.getBytes(UTF8);
            } catch (UnsupportedEncodingException e) {
                LOG.warn("Can't convert string data to binary. ", e);
            }
        }
        return data;
    }

    /**
     * This method take string id from <code>GenericModel</code> object
     *
     * @param model <code>GenericModel</code> object
     * @return the id as string type
     */
    public static String idToString(GenericModel<?> model) {
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
