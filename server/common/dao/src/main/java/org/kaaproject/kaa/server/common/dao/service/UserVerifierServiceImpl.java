/*
 * Copyright 2014 CyberVision, Inc.
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

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import java.util.List;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.dao.UserVerifierService;
import org.kaaproject.kaa.server.common.dao.impl.UserVerifierDao;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserVerifierServiceImpl implements UserVerifierService {

    private static final Logger LOG = LoggerFactory.getLogger(UserVerifierServiceImpl.class);

    @Autowired
    private UserVerifierDao<UserVerifier> userVerifierDao;

    @Override
    public List<UserVerifierDto> findUserVerifiersByAppId(String appId) {
        LOG.debug("Find user verifiers by application id [{}]", appId);
        return convertDtoList(userVerifierDao.findByAppId(appId));
    }

    @Override
    public UserVerifierDto findUserVerifiersByAppIdAndVerifierId(String appId, int verifierId) {
        LOG.debug("Find user verifier by application id [{}] and verifier id [{}]", appId, verifierId);
        return getDto(userVerifierDao.findByAppIdAndVerifierId(appId, verifierId));
    }

    @Override
    public UserVerifierDto findUserVerifierById(String id) {
        LOG.debug("Find user verifier by id  [{}]", id);
        return getDto(userVerifierDao.findById(id));
    }

    @Override
    public UserVerifierDto saveUserVerifier(UserVerifierDto logAppenderDto) {
        LOG.debug("Save user verifier [{}]", logAppenderDto);
        UserVerifierDto saved = null;
        if (logAppenderDto != null) {
            logAppenderDto.setCreatedTime(System.currentTimeMillis());
            saved = getDto(userVerifierDao.save(new UserVerifier(logAppenderDto)));
        }
        return saved;
    }
}
