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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModelUtils {

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

    public static String binaryToString(byte[] data) {
        String body = null;
        if(data!= null) {
            try {
                body = new String(data, UTF8);
            } catch (UnsupportedEncodingException e) {
                LOG.warn("Can't convert binary data to string.");
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
                LOG.warn("Can't convert string data to binary.");
            }
        }
        return data;
    }
}
