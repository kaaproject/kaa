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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateHash;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointNotification;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.Notification;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("#{sql_dao[dao_max_wait_time]}")
    private int waitSeconds;
    @Autowired
    private TopicDao<Topic> topicDao;
    @Autowired
    private NotificationSchemaDao<NotificationSchema> notificationSchemaDao;

    private EndpointProfileDao<EndpointProfile> endpointProfileDao;
    private NotificationDao<Notification> notificationDao;
    private EndpointNotificationDao<EndpointNotification> unicastNotificationDao;

    // 7 days
    private static final int TTL = 7 * 24 * 3600 * 1000;

    @Override
    public NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchemaDto) {
        validateNotificationSchemaObject(notificationSchemaDto);
        String id = notificationSchemaDto.getId();
        if (StringUtils.isBlank(id)) {
            notificationSchemaDto.setId(null);
            notificationSchemaDto.setCreatedTime(System.currentTimeMillis());
            NotificationSchema foundSchema;
            NotificationTypeDto type = notificationSchemaDto.getType();
            if (type != null) {
                foundSchema = notificationSchemaDao.findLatestNotificationSchemaByAppId(notificationSchemaDto.getApplicationId(), type);
            } else {
                throw new IncorrectParameterException("Invalid Notification type in Notification Schema object.");
            }
            if (foundSchema != null) {
                int lastSchemaVersion = foundSchema.getVersion();
                notificationSchemaDto.setVersion(++lastSchemaVersion);
            } else {
                notificationSchemaDto.incrementVersion();
            }
        } else {
            NotificationSchemaDto oldNotificationSchemaDto = getDto(notificationSchemaDao.findById(id));
            if (oldNotificationSchemaDto != null) {
                oldNotificationSchemaDto.editFields(notificationSchemaDto);
                notificationSchemaDto = oldNotificationSchemaDto;
            } else {
                LOG.error("Can't find notification schema with given id [{}].", id);
                throw new IncorrectParameterException("Invalid notification schema id: " + id);
            }
        }
        return getDto(notificationSchemaDao.save(new NotificationSchema(notificationSchemaDto)));
    }

    @Override
    public UpdateNotificationDto<NotificationDto> saveNotification(NotificationDto dto) {
        validateObject(dto, "Can't save notification. Invalid notification object");
        dto.setId(null);
        UpdateNotificationDto<NotificationDto> updateNotificationDto = null;
        String schemaId = dto.getSchemaId();
        String topicId = dto.getTopicId();
        if (isNotBlank(schemaId) && isNotBlank(topicId)) {
            NotificationSchema schema = notificationSchemaDao.findById(schemaId);
            if (schema != null) {
                dto.setNfVersion(schema.getVersion());
                dto.setApplicationId(schema.getApplicationId());
                dto.setType(schema.getType());
            } else {
                throw new DatabaseProcessingException("Can't find notification schema by id " + schemaId);
            }
            try {
                dto.setBody(serializeNotificationBody(dto, schema));
            } catch (IOException e) {
                LOG.error("Can't serialize notification body using schema. ", e);
                throw new DatabaseProcessingException("Can't serialize notification body using schema: " + schemaId);
            }

            long currentTime = new GregorianCalendar(TimeZone.getTimeZone("UTC")).getTimeInMillis();
            Date expiredAt = dto.getExpiredAt();
            dto.setExpiredAt(expiredAt != null ? expiredAt : new Date(currentTime + TTL));
            dto.setLastTimeModify(new Date(currentTime));
            NotificationDto notificationDto = saveNotificationAndIncTopicSecNum(dto);
            if (notificationDto != null) {
                updateNotificationDto = new UpdateNotificationDto<NotificationDto>();
                updateNotificationDto.setAppId(notificationDto.getApplicationId());
                updateNotificationDto.setTopicId(topicId);
                updateNotificationDto.setPayload(notificationDto);
            }
            return updateNotificationDto;
        } else {
            throw new IncorrectParameterException("Incorrect notification object notification schema id is empty");
        }
    }

    public NotificationDto saveNotificationAndIncTopicSecNum(NotificationDto dto) {
        NotificationDto notificationDto = null;
        Topic topic = topicDao.getNextSeqNumber(dto.getTopicId());
        if (topic != null) {
            dto.setSecNum(topic.getSequenceNumber());
            Notification savedDto = notificationDao.save(dto);
            notificationDto = savedDto != null ? savedDto.toDto() : null;
        } else {
            LOG.warn("Can't find topic by id.");
        }
        return notificationDto;
    }

    private byte[] serializeNotificationBody(NotificationDto nf, NotificationSchema nfSchema) throws IOException {
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(nfSchema.getCtlSchema().getBody());
        String notificationJson = new String(nf.getBody(), Charset.forName("UTF8"));
        GenericRecord notificationAvro = converter.decodeJson(notificationJson);
        return converter.encode(notificationAvro);
    }

    @Override
    public NotificationDto findNotificationById(String id) {
        NotificationDto dto = null;
        LOG.debug("Find notification by id [{}] ", id);
        if (StringUtils.isNotBlank(id)) {
            dto = getDto(notificationDao.findById(id));
        }
        LOG.trace("Found notification object {} by id [{}] ", dto, id);
        return dto;
    }

    @Override
    public List<NotificationDto> findNotificationsByTopicId(String topicId) {
        validateId(topicId, "Can't find notifications. Invalid topic id: " + topicId);
        return convertDtoList(notificationDao.findNotificationsByTopicId(topicId));
    }

    @Override
    public NotificationSchemaDto findNotificationSchemaById(String id) {
        validateId(id, "Can't find notification schema. Invalid notification schema id: " + id);
        return getDto(notificationSchemaDao.findById(id));
    }

    @Override
    public List<NotificationSchemaDto> findNotificationSchemasByAppId(String appId) {
        validateId(appId, "Can't find notification schemas. Invalid application id: " + appId);
        return convertDtoList(notificationSchemaDao.findNotificationSchemasByAppId(appId));
    }

    @Override
    public List<VersionDto> findUserNotificationSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't find schemas. Invalid application id: " + applicationId);
        List<NotificationSchema> notificationSchemas = notificationSchemaDao.findNotificationSchemasByAppIdAndType(applicationId, NotificationTypeDto.USER);
        List<VersionDto> schemas = new ArrayList<>();
        for (NotificationSchema notificationSchema : notificationSchemas) {
            schemas.add(notificationSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public List<VersionDto> findNotificationSchemaVersionsByAppId(
            String applicationId) {
        validateId(applicationId, "Can't find notification schema versions. Invalid application id: " + applicationId);
        List<NotificationSchema> notificationSchemas = notificationSchemaDao.findNotificationSchemasByAppId(applicationId);
        List<VersionDto> schemas = new ArrayList<>();
        for (NotificationSchema notificationSchema : notificationSchemas) {
            schemas.add(notificationSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public void removeNotificationSchemasByAppId(String appId) {
        validateId(appId, "Can't remove notification schemas. Invalid application id: " + appId);
        LOG.debug("Cascade remove corresponding notification to application id [{}]", appId);
        unicastNotificationDao.removeNotificationsByAppId(appId);
        notificationSchemaDao.removeNotificationSchemasByAppId(appId);
    }

    @Override
    public List<NotificationDto> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion) {
        validateSqlId(topicId, "Can't find notifications. Invalid topic id: " + topicId);
        return convertDtoList(notificationDao.findNotificationsByTopicIdAndVersionAndStartSecNum(topicId, seqNum, sysNfVersion, userNfVersion));
    }

    @Override
    public List<NotificationSchemaDto> findNotificationSchemasByAppIdAndType(String appId, NotificationTypeDto type) {
        validateId(appId, "Can't find notification schemas. Invalid application id: " + appId);
        return convertDtoList(notificationSchemaDao.findNotificationSchemasByAppIdAndType(appId, type));
    }

    @Override
    public NotificationSchemaDto findNotificationSchemaByAppIdAndTypeAndVersion(
            String appId, NotificationTypeDto type, int majorVersion) {
        validateId(appId, "Can't find notification schema. Invalid application id: " + appId);
        return getDto(notificationSchemaDao.findNotificationSchemasByAppIdAndTypeAndVersion(appId, type, majorVersion));
    }

    @Override
    public EndpointNotificationDto findUnicastNotificationById(String id) {
        validateId(id, "Can't find unicast notification. Invalid id " + id);
        return getDto(unicastNotificationDao.findById(id));
    }

    @Override
    public UpdateNotificationDto<EndpointNotificationDto> saveUnicastNotification(EndpointNotificationDto dto) {
        validateObject(dto, "Can't save unicast notification. Invalid endpoint notification object.");
        UpdateNotificationDto<EndpointNotificationDto> updateNotificationDto = null;
        NotificationDto notificationDto = dto.getNotificationDto();
        String schemaId = notificationDto.getSchemaId();
        String topicId = notificationDto.getTopicId();
        if(isBlank(schemaId)){
            throw new IncorrectParameterException("Invalid notification schema id: " + schemaId);
        }else if(isBlank(topicId)){
            throw new IncorrectParameterException("Invalid notification topic id: " + schemaId);
        }else{
            byte[] endpointKeyHash = dto.getEndpointKeyHash();
            if(endpointKeyHash != null){
                EndpointProfile ep = endpointProfileDao.findByKeyHash(endpointKeyHash);
                if(ep == null){
                    throw new DatabaseProcessingException("Can't find endpoint profile by hash " + endpointKeyHash);
                }
                if(ep.getSubscriptions()== null || ! ep.getSubscriptions().contains(topicId)){
                    //TODO Error code?
                    throw new DatabaseProcessingException("Endpoint profile is not subscribed to this topic");
                }
            } else {
                throw new IncorrectParameterException("Invalid endpointKeyHash: " + endpointKeyHash);
            }
            notificationDto.setId(null);
            notificationDto.setTopicId(topicId);
            notificationDto.setSecNum(-1);
            NotificationSchema schema = notificationSchemaDao.findById(schemaId);
            if (schema != null) {
                notificationDto.setNfVersion(schema.getVersion());
                notificationDto.setApplicationId(schema.getApplicationId());
                notificationDto.setType(schema.getType());
                try {
                    notificationDto.setBody(serializeNotificationBody(notificationDto, schema));
                } catch (IOException e) {
                    LOG.error("Can't serialize notification body using schema. ", e);
                    throw new DatabaseProcessingException("Can't serialize notification body using schema: " + schemaId);
                }
            } else {
                throw new DatabaseProcessingException("Can't find notification schema by id " + schemaId);
            }
            long currentTime = new GregorianCalendar(TimeZone.getTimeZone("UTC")).getTimeInMillis();
            Date expiredAt = notificationDto.getExpiredAt();
            notificationDto.setExpiredAt(expiredAt != null ? expiredAt : new Date(currentTime + TTL));
            notificationDto.setLastTimeModify(new Date(currentTime));

            EndpointNotificationDto unicast = getDto(unicastNotificationDao.save(dto));
            if (unicast != null && unicast.getNotificationDto() != null) {
                LOG.trace("Saved unicast notifications {}", unicast);
                updateNotificationDto = new UpdateNotificationDto<EndpointNotificationDto>();
                NotificationDto savedDto = unicast.getNotificationDto();
                updateNotificationDto.setAppId(savedDto.getApplicationId());
                updateNotificationDto.setTopicId(savedDto.getTopicId());
                updateNotificationDto.setPayload(unicast);
            }
            return updateNotificationDto;
        }
    }

    @Override
    public List<EndpointNotificationDto> findUnicastNotificationsByKeyHash(final byte[] keyHash) {
        validateHash(keyHash, "Can't find unicast notification. Invalid key hash " + keyHash);
        return convertDtoList(unicastNotificationDao.findNotificationsByKeyHash(keyHash));
    }

    @Override
    public void removeUnicastNotificationsByKeyHash(final byte[] keyHash) {
        validateHash(keyHash, "Can't remove unicast notification. Invalid key hash " + keyHash);
        unicastNotificationDao.removeNotificationsByKeyHash(keyHash);
    }

    @Override
    public void removeUnicastNotificationById(String id) {
        validateId(id, "Can't remove unicast notification. Invalid id " + id);
        unicastNotificationDao.removeById(id);
    }

    private void validateNotificationSchemaObject(NotificationSchemaDto dto) {
        validateObject(dto, "Invalid notification schema object");
        if (isBlank(dto.getApplicationId()) && !isValidId(dto.getApplicationId()) || dto.getType() == null) {
            throw new IncorrectParameterException("Invalid notification schema object. Check type or applicationId.");
        }
    }

    public void setEndpointProfileDao(EndpointProfileDao<EndpointProfile> endpointProfileDao) {
        this.endpointProfileDao = endpointProfileDao;
    }

    public void setNotificationDao(NotificationDao<Notification> notificationDao) {
        this.notificationDao = notificationDao;
    }

    public void setUnicastNotificationDao(EndpointNotificationDao<EndpointNotification> unicastNotificationDao) {
        this.unicastNotificationDao = unicastNotificationDao;
    }
}
