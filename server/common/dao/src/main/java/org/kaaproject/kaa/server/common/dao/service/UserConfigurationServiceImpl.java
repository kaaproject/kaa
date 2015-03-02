package org.kaaproject.kaa.server.common.dao.service;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

@Service
public class UserConfigurationServiceImpl implements UserConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserConfigurationServiceImpl.class);

    private EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao;

    @Override
    public EndpointUserConfigurationDto saveUserConfiguration(EndpointUserConfigurationDto dto) {
        EndpointUserConfigurationDto userConfigurationDto = null;
        if (dto != null) {
            userConfigurationDto = getDto(endpointUserConfigurationDao.save(dto));
        } else {
            LOG.warn("Invalid user configuration object. Object is empty");
        }
        return userConfigurationDto;
    }

    @Override
    public EndpointUserConfigurationDto findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        return getDto(endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userId, appToken, schemaVersion));
    }

    @Override
    public List<EndpointUserConfigurationDto> findUserConfigurationByUserId(String userId) {
        return convertDtoList(endpointUserConfigurationDao.findByUserId(userId));
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        endpointUserConfigurationDao.removeByUserIdAndAppTokenAndSchemaVersion(userId, appToken, schemaVersion);
    }


    public void setEndpointUserConfigurationDao(EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao) {
        this.endpointUserConfigurationDao = endpointUserConfigurationDao;
    }
}
