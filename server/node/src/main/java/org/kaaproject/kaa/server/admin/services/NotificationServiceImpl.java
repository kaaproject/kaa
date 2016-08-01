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

package org.kaaproject.kaa.server.admin.services;

import com.google.common.base.Charsets;
import net.iharder.Base64;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.NotificationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.NotificationService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("notificationService")
public class NotificationServiceImpl extends AbstractAdminService implements NotificationService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    @Autowired
    CtlService ctlService;

    @Override
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getNotificationSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.findNotificationSchemasByAppIdAndType(applicationId, NotificationTypeDto.USER);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<VersionDto> getUserNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getUserNotificationSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getUserNotificationSchemasByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            NotificationSchemaDto notificationSchema = controlService.getNotificationSchema(notificationSchemaId);
            Utils.checkNotNull(notificationSchema);
            checkApplicationId(notificationSchema.getApplicationId());
            return notificationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(notificationSchema.getId())) {
                notificationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(notificationSchema.getApplicationId());
            } else {
                NotificationSchemaDto storedNotificationSchema = controlService.getNotificationSchema(notificationSchema.getId());
                Utils.checkNotNull(storedNotificationSchema);
                checkApplicationId(storedNotificationSchema.getApplicationId());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return controlService.saveNotificationSchema(notificationSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getTopicsByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getTopicByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getTopicByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantTopicByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto getTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            return topic;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(topic.getId())) {
                topic.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(topic.getApplicationId());
            } else {
                throw new KaaAdminServiceException("Unable to edit existing topic!", ServiceErrorCode.INVALID_ARGUMENTS);
            }
            return controlService.editTopic(topic);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.deleteTopicById(topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.addTopicsToEndpointGroup(endpointGroupId, topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.removeTopicsFromEndpointGroup(endpointGroupId, topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            return controlService.editNotification(notification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            EndpointNotificationDto unicastNotification = new EndpointNotificationDto();
            unicastNotification.setEndpointKeyHash(Base64.decode(clientKeyHash.getBytes(Charsets.UTF_8)));
            unicastNotification.setNotificationDto(notification);
            return controlService.editUnicastNotification(unicastNotification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkTopicId(String topicId) throws IllegalArgumentException {
        if (isEmpty(topicId)) {
            throw new IllegalArgumentException("The topicId parameter is empty.");
        }
    }

    private void checkExpiredDate(NotificationDto notification) throws KaaAdminServiceException {
        if (null != notification.getExpiredAt() && notification.getExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("Overdue expiry time for notification!");
        }
    }

    @Override
    public List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            List<NotificationSchemaDto> notificationSchemas = controlService.findNotificationSchemasByAppIdAndType(applicationId,
                    NotificationTypeDto.USER);
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(notificationSchemas.size());
            for (NotificationSchemaDto notificationSchema : notificationSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(notificationSchema);
                RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(notificationSchema.getCtlSchemaId(), null);
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaViewDto getNotificationSchemaView(String notificationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            NotificationSchemaDto notificationSchema = getNotificationSchema(notificationSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(notificationSchema.getCtlSchemaId());
            NotificationSchemaViewDto notificationSchemaViewDto = new NotificationSchemaViewDto(notificationSchema, toCtlSchemaForm(ctlSchemaDto,
                    ConverterType.FORM_AVRO_CONVERTER));
            return notificationSchemaViewDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaViewDto saveNotificationSchemaView(NotificationSchemaViewDto notificationSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            NotificationSchemaDto notificationSchema = notificationSchemaView.getSchema();
            String applicationId = notificationSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = notificationSchema.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (notificationSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = notificationSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(),
                            metaInfo.getVersion(),
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    notificationSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = ctlService.saveCTLSchemaForm(notificationSchemaView.getCtlSchemaForm(), ConverterType.FORM_AVRO_CONVERTER);
                    notificationSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            NotificationSchemaDto savedNotificationSchema = saveNotificationSchema(notificationSchema);
            return getNotificationSchemaView(savedNotificationSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaViewDto createNotificationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
        LOG.error("createNotificationSchemaFormCtlSchema [{}]", ctlSchemaForm.getSchema().getDisplayString());
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            NotificationSchemaDto notificationSchema = new NotificationSchemaDto();
            notificationSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            notificationSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            notificationSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = ctlService.saveCTLSchemaForm(ctlSchemaForm, ConverterType.FORM_AVRO_CONVERTER);
            notificationSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            NotificationSchemaDto savedNotificationSchema = saveNotificationSchema(notificationSchema);
            return getNotificationSchemaView(savedNotificationSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(notificationData);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            byte[] body = converter.encodeToJsonBytes(record);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.editNotification(notification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, RecordField notificationData)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(notificationData);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            byte[] body = converter.encodeToJsonBytes(record);
            return sendUnicastNotification(notification, clientKeyHash, body);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

}
