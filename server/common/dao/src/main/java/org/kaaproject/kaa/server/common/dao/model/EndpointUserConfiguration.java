package org.kaaproject.kaa.server.common.dao.model;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;

public interface EndpointUserConfiguration extends ToDto<EndpointUserConfigurationDto> {

    String getUserId();

    void setUserId(String userId);

    String getAppToken();

    public void setAppToken(String appToken);

    Integer getSchemaVersion();

    void setSchemaVersion(Integer schemaVersion);
}
