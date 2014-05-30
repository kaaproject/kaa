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

import com.mongodb.MongoException;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.HistoryDao;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.History;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;

@Service
public class HistoryServiceImpl implements HistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryServiceImpl.class);

    @Value("${dao.max.wait.time}")
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
        if (isValidObject(historyDto)) {
            LOG.debug("History dto object is valid. Saving history...");
            String applicationId = historyDto.getApplicationId();
            if (StringUtils.isNotEmpty(applicationId)) {
                savedDto = saveHistory(historyDto, applicationId);
            } else {
                LOG.debug("Incorrect application id, can't save history.");
            }
        } else {
            LOG.info("Invalid HistoryDto object. Can't save object.");
        }
        return savedDto;
    }


    private HistoryDto saveHistory(HistoryDto historyDto, String appId) {
        HistoryDto savedDto = null;
        try {
            History savedHistory = saveHistoryAndIncrementSeqNum(historyDto, appId);
            if (savedHistory != null) {
                savedDto = savedHistory.toDto();
            }
        } catch (MongoException.DuplicateKey ex) {
            LOG.debug("Catch duplicate key exception with id: [{}]", historyDto.getId());
            savedDto = saveHistory(historyDto, appId);
        }
        return savedDto;
    }

    private History saveHistoryAndIncrementSeqNum(HistoryDto historyDto, String applicationId) {
        History saved = null;
        Application application = getNextSequenceNumber(applicationId);
        if (application != null && application.getUpdate() != null) {
            Update update = application.getUpdate();
            int sequenceNumber = update.getSequenceNumber();
            historyDto.setId(getId(applicationId, sequenceNumber));
            historyDto.setSequenceNumber(sequenceNumber);
            historyDto.setLastModifyTime(System.currentTimeMillis());
            saved = historyDao.save(new History(historyDto));
            if (saved != null) {
                applicationDao.updateSeqNumber(applicationId);
            } else {
                LOG.debug("Can't save history with id [{}] .", historyDto.getId());
            }
        } else {
            LOG.debug("Can't get sequence number for application id [{}] .", applicationId);
        }
        return saved;
    }

    private Application getNextSequenceNumber(String appId) {
        long maxLatency = waitSeconds * 1000;
        Application application = null;
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while (application == null) {
            if ((endTime - startTime) < maxLatency) {
                application = applicationDao.getNextSeqNumber(appId);
            } else {
                application = applicationDao.forceNextSeqNumber(appId);
                break;
            }
            endTime = System.currentTimeMillis();
        }
        return application;
    }

    private String getId(String appId, int sequenceNumber) {
        return new StringBuilder().append(appId).append("_").append(sequenceNumber).toString();
    }

}
