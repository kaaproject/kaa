package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.utils.Bytes;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointGroupState;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEventClassFamilyVersionState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
}
