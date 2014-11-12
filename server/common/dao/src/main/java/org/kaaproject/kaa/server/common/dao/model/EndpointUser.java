package org.kaaproject.kaa.server.common.dao.model;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointUserDto;

public interface EndpointUser extends ToDto<EndpointUserDto>{

    List<String> getEndpointIds();

    void setEndpointIds(List<String> endpointIds);

    String getId();

}
