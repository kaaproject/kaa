package org.kaaproject.kaa.server.common.dao.model;

import org.kaaproject.kaa.common.dto.HasVersion;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;

public interface Credentials extends ToDto<CredentialsDto> {

    String getId();
    byte[] getCredentialsBody();
    CredentialsStatus getStatus();
}
