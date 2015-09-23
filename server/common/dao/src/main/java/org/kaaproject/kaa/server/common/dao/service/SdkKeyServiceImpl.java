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
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.server.common.dao.SdkKeyService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.impl.SdkKeyDao;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SdkKeyServiceImpl implements SdkKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkKeyServiceImpl.class);

    @Autowired
    private SdkKeyDao<SdkKey> sdkKeyDao;

    @Override
    public SdkPropertiesDto findSdkKeyByToken(String token) {
        SdkPropertiesDto sdkPropertiesDto = null;
        if (Validator.isValidId(token)) {
            sdkPropertiesDto = DaoUtil.getDto(sdkKeyDao.findSdkKeyByToken(token));
        }
        return sdkPropertiesDto;
    }

    @Override
    public SdkPropertiesDto saveSdkKey(SdkPropertiesDto sdkPropertiesDto) {
        SdkPropertiesDto saved = null;

        if (Validator.isValidSqlObject(sdkPropertiesDto)) {
            if (StringUtils.isNotBlank(sdkPropertiesDto.getId())) {
                SdkKey entity = new SdkKey(sdkPropertiesDto);
                SdkKey loaded = sdkKeyDao.findSdkKeyByToken(entity.getToken());

                LOG.debug("Saving SDK profile [{}] for application [{}]", entity.getToken(), sdkPropertiesDto.getApplicationId());

                if (loaded == null || loaded.getStringId().equals(entity.getStringId())) {
                    saved = DaoUtil.getDto(sdkKeyDao.save(entity));
                } else {
                    throw new IncorrectParameterException("An SDK profile with token [" + entity.getToken() + "] already exists.");
                }
            } else {
                SdkKey entity = new SdkKey(sdkPropertiesDto);
                SdkKey loaded = sdkKeyDao.findSdkKeyByToken(entity.getToken());

                if (loaded == null) {
                    saved = DaoUtil.getDto(sdkKeyDao.save(entity));
                } else {
                    saved = DaoUtil.getDto(loaded);
                }
            }
        }

        return saved;
    }

    @Override
    public List<SdkPropertiesDto> findSdkKeysByApplicationId(String applicationId) {
        Validator.validateId(applicationId, "Unable to find SDK profiles. Invalid application ID: " + applicationId);
        return DaoUtil.convertDtoList(sdkKeyDao.findSdkKeysByApplicationId(applicationId));
    }

    @Override
    public void removeSdkProfileById(String id) {
        Validator.validateId(id, "Unable to remove SDK profile. Invalid SDK profile ID: " + id);
        sdkKeyDao.removeById(id);
        LOG.debug("Removed SDK profile [{}]", id);
    }
}
