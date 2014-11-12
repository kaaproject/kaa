package org.kaaproject.kaa.server.common.dao.model;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;

public interface EndpointProfile extends ToDto<EndpointProfileDto>{

    byte[] getEndpointKey();

    String getId();

    String getEndpointUserId();

    void setEndpointUserId(String id);

    List<String> getSubscriptions();

}
