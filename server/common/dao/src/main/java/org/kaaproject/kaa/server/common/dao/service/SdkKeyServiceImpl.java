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

import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.server.common.dao.SdkKeyService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.SdkKeyDao;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidSqlObject;

@Service
@Transactional
public class SdkKeyServiceImpl implements SdkKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkKeyServiceImpl.class);

    @Autowired
    private SdkKeyDao<SdkKey> sdkKeyDao;

    @Override
    public SdkPropertiesDto findSdkKeyByToken(String token) {
        SdkPropertiesDto sdkPropertiesDto = null;
        if (isValidId(token)) {
            sdkPropertiesDto = getDto(sdkKeyDao.findSdkKeyByToken(token));
        }
        return sdkPropertiesDto;
    }

    @Override
    public SdkPropertiesDto saveSdkKey(SdkPropertiesDto sdkPropertiesDto) {
        SdkPropertiesDto savedSdkPropertiesDto = null;
        if (isValidSqlObject(sdkPropertiesDto)) {
            if (isNotBlank(sdkPropertiesDto.getId())) {
                SdkKey sdkKeyToSave = new SdkKey(sdkPropertiesDto);
                LOG.debug("Update application with id [{}] for token [{}]", sdkPropertiesDto.getApplicationId(),
                        sdkKeyToSave.getToken());
                SdkKey checkSdkKey = sdkKeyDao.findSdkKeyByToken(sdkKeyToSave.getToken());
                if (checkSdkKey == null || String.valueOf(sdkKeyToSave.getId()).equals(String.valueOf(checkSdkKey.getId()))) {
                    savedSdkPropertiesDto = getDto(sdkKeyDao.save(sdkKeyToSave));
                } else {
                    throw new IncorrectParameterException("Can't add two sdk keys with the same token values: " + sdkKeyToSave.getToken());
                }
                return savedSdkPropertiesDto;
            }
            savedSdkPropertiesDto = getDto(sdkKeyDao.save(new SdkKey(sdkPropertiesDto)));
        }
        return savedSdkPropertiesDto;
    }
}
