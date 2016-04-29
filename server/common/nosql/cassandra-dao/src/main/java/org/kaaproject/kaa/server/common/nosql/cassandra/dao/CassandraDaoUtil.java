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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEndpointGroupState;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEventClassFamilyVersionState;

import com.datastax.driver.core.utils.Bytes;

public class CassandraDaoUtil {

    private CassandraDaoUtil() {
    }

    /**
     * Specific method for converting list of <code>EndpointGroupStateDto</code> objects
     * to list of model objects <code>CassandraEndpointGroupState</code>.
     *
     * @param stateDtoList the state dto list
     * @return converted list of <code>CassandraEndpointGroupState</code> objects
     */
    public static List<CassandraEndpointGroupState> convertDtoToModelList(List<EndpointGroupStateDto> stateDtoList) {
        List<CassandraEndpointGroupState> states = null;
        if (stateDtoList != null && !stateDtoList.isEmpty()) {
            states = new ArrayList<>();
            for (EndpointGroupStateDto dto : stateDtoList) {
                CassandraEndpointGroupState state = new CassandraEndpointGroupState();
                state.setConfigurationId(dto.getConfigurationId());
                state.setEndpointGroupId(dto.getEndpointGroupId());
                state.setProfileFilterId(dto.getProfileFilterId());
                states.add(state);
            }
        }
        return states;
    }

    /**
     * Specific method for converting list of <code>EventClassFamilyVersionStateDto</code> objects
     * to list of model objects <code>CassandraEventClassFamilyVersionState</code>
     *
     * @param stateDtoList
     * @return converted list of <code>CassandraEventClassFamilyVersionState</code> objects
     */
    public static List<CassandraEventClassFamilyVersionState> convertECFVersionDtoToModelList(List<EventClassFamilyVersionStateDto> stateDtoList) {
        List<CassandraEventClassFamilyVersionState> states = null;
        if (stateDtoList != null && !stateDtoList.isEmpty()) {
            states = new ArrayList<>();
            for (EventClassFamilyVersionStateDto dto : stateDtoList) {
                CassandraEventClassFamilyVersionState state = new CassandraEventClassFamilyVersionState();
                state.setEcfId(dto.getEcfId());
                state.setVersion(dto.getVersion());
                states.add(state);
            }
        }
        return states;
    }

    /**
     * This method convert  byte array to ByteBuffer object
     *
     * @param array
     * @return the ByteBuffer object or null
     */
    public static ByteBuffer getByteBuffer(byte[] array) {
        ByteBuffer bb = null;
        if (array != null) {
            bb = ByteBuffer.wrap(array);
        }
        return bb;
    }

    /**
     * This method convert ByteBuffer object to byte array
     *
     * @param byteBuffer
     * @return the byte array or null
     */
    public static byte[] getBytes(ByteBuffer byteBuffer) {
        byte[] array = null;
        if (byteBuffer != null) {
            array = Bytes.getArray(byteBuffer);
        }
        return array;
    }

    /**
     * This method convert string id to substring fields divided by
     * {@link org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants#KEY_DELIMITER }
     *
     * @param id
     * @return the string array or null
     */
    public static String[] parseId(String id) {
        String[] ids = null;
        if (isNotBlank(id) && id.contains(CassandraModelConstants.KEY_DELIMITER)) {
            ids = id.split(CassandraModelConstants.KEY_DELIMITER);
        }
        return ids;
    }

    /**
     * This method convert ByteBuffer object to string representation,
     * if endpointKeyHash eq null, than return null
     *
     * @param endpointKeyHash
     * @return the String representation of endpoint key hash
     */
    public static String convertKeyHashToString(ByteBuffer endpointKeyHash) {
        String id = null;
        if (endpointKeyHash != null) {
            id = Bytes.toHexString(endpointKeyHash);
        }
        return id;
    }

    public static String convertKeyHashToString(byte[] endpointKeyHash) {
        String id = null;
        if (endpointKeyHash != null) {
            id = Bytes.toHexString(endpointKeyHash);
        }
        return id;
    }

    /**
     * This method convert string representation of endpoint key hash to ByteBuffer object
     * if id eq null, than return null
     *
     * @param id
     * @return the ByteBuffer object
     */
    public static ByteBuffer convertStringToKeyHash(String id) {
        ByteBuffer endpointKeyHash =null;
        if (id != null && id.length() != 0) {
            endpointKeyHash = Bytes.fromHexString(id);
        }
        return endpointKeyHash;
    }
}
