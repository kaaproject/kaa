/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.SdkProfileDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SdkProfileServiceImpl implements SdkProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkProfileServiceImpl.class);

    @Autowired
    private SdkProfileDao<SdkProfile> sdkProfileDao;

    private EndpointProfileDao<EndpointProfile> endpointProfileDao;

    public EndpointProfileDao<EndpointProfile> getEndpointProfileDao() {
        return endpointProfileDao;
    }

    public void setEndpointProfileDao(EndpointProfileDao<EndpointProfile> endpointProfileDao) {
        this.endpointProfileDao = endpointProfileDao;
    }

    @Override
    public SdkProfileDto saveSdkProfile(SdkProfileDto sdkPropertiesDto) {
        SdkProfileDto saved = null;

        if (Validator.isValidSqlObject(sdkPropertiesDto)) {
            if (StringUtils.isNotBlank(sdkPropertiesDto.getId())) {
                SdkProfile entity = new SdkProfile(sdkPropertiesDto);
                SdkProfile loaded = sdkProfileDao.findSdkProfileByToken(entity.getToken());

                LOG.debug("Saving SDK profile [{}] for application [{}]", entity.getToken(), sdkPropertiesDto.getApplicationId());

                if (loaded == null || loaded.getStringId().equals(entity.getStringId())) {
                    saved = DaoUtil.getDto(sdkProfileDao.save(entity));
                } else {
                    throw new IncorrectParameterException("An SDK profile with token [" + entity.getToken() + "] already exists.");
                }
            } else {
                SdkProfile entity = new SdkProfile(sdkPropertiesDto);
                SdkProfile loaded = sdkProfileDao.findSdkProfileByToken(entity.getToken());

                if (loaded == null) {
                    saved = DaoUtil.getDto(sdkProfileDao.save(entity));
                } else {
                    saved = DaoUtil.getDto(loaded);
                }
            }
        }

        return saved;
    }

    @Override
    public SdkProfileDto findSdkProfileById(String id) {
        SdkProfileDto sdkPropertiesDto = null;
        if (Validator.isValidId(id)) {
            sdkPropertiesDto = DaoUtil.getDto(sdkProfileDao.findById(id));
        }
        return sdkPropertiesDto;
    }

    @Override
    public SdkProfileDto findSdkProfileByToken(String token) {
        SdkProfileDto sdkPropertiesDto = null;
        if (Validator.isValidId(token)) {
            sdkPropertiesDto = DaoUtil.getDto(sdkProfileDao.findSdkProfileByToken(token));
        }
        return sdkPropertiesDto;
    }

    @Override
    public List<SdkProfileDto> findSdkProfilesByApplicationId(String applicationId) {
        Validator.validateId(applicationId, "Unable to find SDK profiles. Invalid application ID: " + applicationId);
        return DaoUtil.convertDtoList(sdkProfileDao.findSdkProfileByApplicationId(applicationId));
    }

    @Override
    public void removeSdkProfileById(String id) {
        Validator.validateId(id, "Unable to remove SDK profile. Invalid SDK profile ID: " + id);
        sdkProfileDao.removeById(id);
        LOG.debug("Removed SDK profile [{}]", id);
    }

    @Override
    public boolean isSdkProfileUsed(String token) {
        return endpointProfileDao.checkSdkToken(token);
    }
}
