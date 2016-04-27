/*
 * Copyright 2014-2016 CyberVision, Inc.
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
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidSqlId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidSqlObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;

import java.util.List;

import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.impl.HistoryDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryServiceImpl.class);

    @Value("#{sql_dao[dao_max_wait_time]}")
    private int waitSeconds;

    @Autowired
    private HistoryDao<History> historyDao;

    @Autowired
    private ApplicationDao<Application> applicationDao;


    @Override
    public List<HistoryDto> findHistoriesByAppId(String appId) {
        LOG.debug("Find history by application id [{}]", appId);
        validateId(appId, "Can't find history by application id. Invalid application id " + appId);
        return convertDtoList(historyDao.findByAppId(appId));
    }

    @Override
    public HistoryDto findHistoryBySeqNumber(String appId, int seqNum) {
        LOG.debug("Find history by application id [{}] and sequence number {}", appId, seqNum);
        validateId(appId, "Can't find history by application id and sequence number. Invalid application id " + appId);
        return getDto(historyDao.findBySeqNumber(appId, seqNum));
    }

    @Override
    public List<HistoryDto> findHistoriesBySeqNumberStart(String appId, int startSeqNum) {
        LOG.debug("Find history range by application id [{}] and start sequence number {}", appId, startSeqNum);
        validateId(appId, "Can't find history by application id and start sequence number. Invalid application id " + appId);
        return convertDtoList(historyDao.findBySeqNumberStart(appId, startSeqNum));
    }

    @Override
    public List<HistoryDto> findHistoriesBySeqNumberRange(String appId, int startSeqNum, int endSeqNum) {
        LOG.debug("Find history range by application id [{}] and start sequence number {} end sequence number {} ",
                appId, startSeqNum, endSeqNum);

        validateId(appId, "Can't find history by application id and sequence number range. Invalid application id " + appId);
        return convertDtoList(historyDao.findBySeqNumberRange(appId, startSeqNum, endSeqNum));
    }

    @Override
    public HistoryDto saveHistory(HistoryDto historyDto) {
        HistoryDto savedDto = null;
        if (isValidSqlObject(historyDto)) {
            LOG.debug("History dto object is valid. Saving history...");
            String applicationId = historyDto.getApplicationId();
            if (isValidSqlId(applicationId)) {
                Application application = applicationDao.getNextSeqNumber(applicationId);
                if (application != null) {
                    int sequenceNumber = application.getSequenceNumber();
                    historyDto.setSequenceNumber(sequenceNumber);
                    historyDto.setLastModifyTime(System.currentTimeMillis());
                    History savedHistory = historyDao.persist(new History(historyDto));
                    savedDto = savedHistory != null ? savedHistory.toDto() : null;
                } else {
                    LOG.debug("Can't get sequence number for application id [{}] .", applicationId);
                }
            } else {
                LOG.debug("Incorrect application id, can't save history.");
            }
        } else {
            LOG.info("Invalid HistoryDto object. Can't save object.");
        }
        return savedDto;
    }

}
