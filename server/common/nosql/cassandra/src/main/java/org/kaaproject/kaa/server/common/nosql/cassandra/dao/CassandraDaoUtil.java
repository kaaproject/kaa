package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.utils.Bytes;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEndpointGroupState;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEventClassFamilyVersionState;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class CassandraDaoUtil {

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

    public static String getStringId(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    public static UUID getUuidId(String id) {
        return id != null ? UUID.fromString(id) : null;
    }

    public static ByteBuffer getByteBuffer(byte[] array) {
        ByteBuffer bb = null;
        if (array != null) {
            bb = ByteBuffer.wrap(array);
        }
        return bb;
    }

    public static byte[] getBytes(ByteBuffer byteBuffer) {
        byte[] array = null;
        if (byteBuffer != null) {
            array = Bytes.getArray(byteBuffer);
        }
        return array;
    }

    public static String[] parseId(String id) {
        String[] ids = null;
        if (isNotBlank(id) && id.contains(CassandraModelConstants.KEY_DELIMITER)) {
            ids = id.split(CassandraModelConstants.KEY_DELIMITER);
        }
        return ids;
    }

    public static String convertKeyHashToString(ByteBuffer endpointKeyHash) {
        String id = null;
        if (endpointKeyHash != null) {
            id = Bytes.toHexString(endpointKeyHash);
        }
        return id;
    }

    public static ByteBuffer convertStringToKeyHash(String id) {
        ByteBuffer endpointKeyHash =null;
        if (id != null && id.length() != 0) {
            endpointKeyHash = Bytes.fromHexString(id);
        }
        return endpointKeyHash;
    }
}
