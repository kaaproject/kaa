package org.kaaproject.kaa.server.node.service.credentials;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.springframework.stereotype.Service;

@Service
public class DefaultInternalCredentialsService implements InternalCredentialsService {

    @Override
    public CredentialsDto provisionCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CredentialsDto lookupCredentials(String applicationId, String credentialsId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException {
        // TODO Auto-generated method stub
        
    }

}
