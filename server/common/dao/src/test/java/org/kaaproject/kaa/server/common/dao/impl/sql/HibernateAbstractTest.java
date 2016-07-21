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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventMap;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.kaaproject.kaa.server.common.dao.model.sql.Change;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClass;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.kaaproject.kaa.server.common.dao.model.sql.EventSchemaVersion;
import org.kaaproject.kaa.server.common.dao.model.sql.History;
import org.kaaproject.kaa.server.common.dao.model.sql.LogAppender;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.kaaproject.kaa.server.common.dao.model.sql.User;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HibernateAbstractTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateAbstractTest.class);

    protected Tenant generateTenant() {
        LOG.debug("Generate tenant...");
        Tenant tenant = new Tenant();
        tenant.setName("Test tenant" + RANDOM.nextInt());
        tenant = tenantDao.save(tenant);
        LOG.debug("Generate tenant {}", tenant);
        return tenant;
    }

    protected Change generateChange() {
        LOG.debug("Generate change...");
        Change change = new Change();
        change.setConfigurationId(RANDOM.nextLong());
        change.setConfigurationVersion(RANDOM.nextInt(3000));
        change = historyDao.save(change, Change.class);
        LOG.debug("Generated change {}", change);
        return change;
    }

    protected List<History> generateHistory(Application app, int count) {
        LOG.debug("Generate history...");
        List<History> histories = new ArrayList<>();
        if (app == null) {
            app = generateApplication(null);
        }
        for (int i = 0; i < count; i++) {
            History history = new History();
            history.setApplication(app);
            history.setLastModifyTime(System.currentTimeMillis());
            history.setSequenceNumber(i + 1);
            history.setChange(generateChange());
            history = historyDao.save(history);
            histories.add(history);
            LOG.debug("Generated history {}", history);
        }
        return histories;
    }

    protected User generateUser(Tenant tenant, KaaAuthorityDto authority) {
        LOG.debug("Generate user...");
        if (tenant == null) {
            tenant = generateTenant();
        }
        User user = new User();
        user.setExternalUid(UUID.randomUUID().toString());
        user.setTenant(tenant);
        if (authority == null) {
            authority = KaaAuthorityDto.KAA_ADMIN;
        }
        user.setAuthority(authority);
        user.setUsername("TestUserName");
        user = userDao.save(user);
        LOG.debug("Generated user {}", user);
        return user;
    }

    protected Application generateApplication(Tenant tenant) {
        LOG.debug("Generate application...");
        if (tenant == null) {
            tenant = generateTenant();
        }
        Application app = new Application();
        app.setName("Test app name" + UUID.randomUUID().toString());
        app.setTenant(tenant);
        app.setApplicationToken(UUID.randomUUID().toString());
        app.setSequenceNumber(RANDOM.nextInt());
        app = applicationDao.save(app);
        LOG.debug("Generated application {}", app);
        return app;
    }

    protected EndpointGroup generateEndpointGroup(Application app, Set<Topic> topics) {
        EndpointGroup group = new EndpointGroup();
        if (app == null) {
            app = generateApplication(null);
        }
        group.setApplication(app);
        group.setName("GROUP_ALL_" + RANDOM.nextInt());
        group.setWeight(RANDOM.nextInt());
        group.setTopics(topics);
        return endpointGroupDao.save(group);
    }

    protected List<ConfigurationSchema> generateConfSchema(Application app, int count) {
        List<ConfigurationSchema> schemas = Collections.emptyList();
        try {
            if (app == null) {
                app = generateApplication(null);
            }
            CTLSchema ctlSchema = generateCTLSchema(DEFAULT_FQN, 1, app.getTenant(), null);
            ConfigurationSchema schema;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schema = new ConfigurationSchema();
                schema.setApplication(app);
                schema.setCreatedUsername("Test User");
                schema.setCtlSchema(ctlSchema);
                schema.setVersion(i + 1);
                schema.setName("Test Name");
                schema = configurationSchemaDao.save(schema);
                Assert.assertNotNull(schema);
                schemas.add(schema);
            }
        } catch (Exception e) {
            LOG.error("Can't generate configuration schemas {}", e);
            Assert.fail("Can't generate configuration schemas." + e.getMessage());
        }
        return schemas;
    }

    protected List<Configuration> generateConfiguration(ConfigurationSchema schema, EndpointGroup group, int count, UpdateStatus status) {
        List<Configuration> configs = Collections.emptyList();
        try {
            if (schema == null) {
                schema = generateConfSchema(null, 1).get(0);
            }
            if (group == null) {
                group = generateEndpointGroup(schema.getApplication(), null);
            }
            Assert.assertNotNull(schema);
            configs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Configuration dto = new Configuration();
                dto.setId(null);
                dto.setStatus(status != null ? status : UpdateStatus.INACTIVE);
                dto.setConfigurationBody(new byte[]{0, 2, 3, 4,});
                dto.setConfigurationSchema(schema);
                dto.setSequenceNumber(i);
                dto.setSchemaVersion(i + 1);
                dto.setApplication(schema.getApplication());
                dto.setEndpointGroup(group);
                Configuration saved = configurationDao.save(dto);
                Assert.assertNotNull(saved);
                configs.add(saved);
            }
        } catch (Exception e) {
            LOG.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations." + e.getMessage());
        }
        return configs;
    }

    protected List<EndpointProfileSchema> generateProfSchema(Application app, int count) {
        List<EndpointProfileSchema> schemas = Collections.emptyList();
        try {
            if (app == null) {
                app = generateApplication(null);
            }
            CTLSchema ctlSchema = generateCTLSchema(DEFAULT_FQN, 1, app.getTenant(), null);
            EndpointProfileSchema schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new EndpointProfileSchema();
                schemaDto.setApplication(app);
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setCtlSchema(ctlSchema);
                schemaDto.setVersion(i + 1);
                schemaDto.setName("Test Name");
                schemaDto = profileSchemaDao.save(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOG.error("Can't generate profile schema {}", e);
            Assert.fail("Can't generate profile schema." + e.getMessage());
        }
        return schemas;
    }

    protected CTLSchema generateCTLSchema(String fqn, int version, Tenant tenant, CTLSchemaScopeDto scope) {
        if (scope == null) {
            if (tenant == null) {
                scope = CTLSchemaScopeDto.SYSTEM;
            } else {
                scope = CTLSchemaScopeDto.TENANT;
            }
        }
        CTLSchemaMetaInfo metaInfo = new CTLSchemaMetaInfo();
        metaInfo.setFqn(fqn);
        metaInfo.setTenant(tenant);
        metaInfo = ctlSchemaMetaInfoDao.save(metaInfo);
        CTLSchema ctlSchema = new CTLSchema();
        ctlSchema.setMetaInfo(metaInfo);
        ctlSchema.setVersion(version);
        ctlSchema.setBody(UUID.randomUUID().toString());
        ctlSchema.setDependencySet(new HashSet<CTLSchema>());
        ctlSchema = ctlSchemaDao.save(ctlSchema);
        return ctlSchema;
    }

    protected List<NotificationSchema> generateNotificationSchema(Application app, int ctlVersion, int count, NotificationTypeDto type) {
        List<NotificationSchema> schemas = Collections.emptyList();
        try {
            if (app == null) {
                app = generateApplication(null);
            }
            CTLSchema ctlSchema = generateCTLSchema(DEFAULT_FQN, ctlVersion, app.getTenant(), null);
            NotificationSchema schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new NotificationSchema();
                schemaDto.setApplication(app);
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setCtlSchema(ctlSchema);
                schemaDto.setVersion(i + 1);
                schemaDto.setName("Test Name");
                schemaDto.setType(type);
                schemaDto = notificationSchemaDao.save(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOG.error("Can't generate profile schema {}", e);
            Assert.fail("Can't generate profile schema." + e.getMessage());
        }
        return schemas;
    }

    protected List<ProfileFilter> generateFilter(EndpointProfileSchema schema, ServerProfileSchema srvSchema, EndpointGroup group, int count, UpdateStatus status) {
        return generateFilter(generateApplication(null), schema, srvSchema, group, count, status);
    }

    protected List<ProfileFilter> generateFilter(Application app, EndpointProfileSchema schema, ServerProfileSchema srvSchema, EndpointGroup group, int count, UpdateStatus status) {
        if (schema == null) {
            schema = generateProfSchema(app, 1).get(0);
        }

        if (srvSchema == null) {
            srvSchema = new ServerProfileSchema(generateServerProfileSchema(app.getStringId(), app.getTenant().getStringId()));
        }
        if (group == null) {
            group = generateEndpointGroup(app, null);
        }
        List<ProfileFilter> filters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ProfileFilter dto = new ProfileFilter();
            dto.setId(null);
            dto.setStatus(status != null ? status : UpdateStatus.INACTIVE);
            dto.setEndpointGroup(group);
            dto.setEndpointProfileSchema(schema);
            dto.setServerProfileSchema(srvSchema);
            dto.setSequenceNumber(i);
            dto.setApplication(app);
            ProfileFilter saved = profileFilterDao.save(dto);
            Assert.assertNotNull(saved);
            filters.add(saved);
        }
        return filters;
    }

    protected List<ProfileFilter> generateFilterWithoutSchemaGeneration(EndpointProfileSchema schema, ServerProfileSchema srvSchema, EndpointGroup group, int count, UpdateStatus status) {
        Application app = null;
        if (schema != null) {
            app = schema.getApplication();
        } else if (srvSchema != null) {
            app = srvSchema.getApplication();
        }
        if (group == null) {
            group = generateEndpointGroup(app, null);
        }
        List<ProfileFilter> filters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ProfileFilter dto = new ProfileFilter();
            dto.setId(null);
            dto.setStatus(status != null ? status : UpdateStatus.INACTIVE);
            dto.setEndpointGroup(group);
            dto.setEndpointProfileSchema(schema);
            dto.setServerProfileSchema(srvSchema);
            dto.setSequenceNumber(i);
            dto.setApplication(app);
            ProfileFilter saved = profileFilterDao.save(dto);
            Assert.assertNotNull(saved);
            filters.add(saved);
        }
        return filters;
    }


    protected Topic generateTopic(Application app, TopicTypeDto type, String topicName) {
        Topic topic = new Topic();
        if (topicName != null && !topicName.isEmpty()) {
            topic.setName(topicName);
        } else {
            topic.setName("Generated Topic name");
        }
        if (app == null) {
            app = generateApplication(null);
        }
        topic.setApplication(app);
        if (type == null) {
            type = TopicTypeDto.MANDATORY;
        }
        topic.setType(type);
        return topicDao.save(topic);
    }

    protected LogAppender generateLogAppender(Application app) {
        LogAppender appender = new LogAppender();
        if (app == null) {
            app = generateApplication(null);
        }
        appender.setApplication(app);
        appender.setMinLogSchemaVersion(1);
        appender.setMaxLogSchemaVersion(2);
        return appenderDao.save(appender);
    }

    protected List<EventClassFamily> generateEventClassFamily(Tenant tenant, int count) {
        int eventSchemaVersionsCount = 2;
        if (tenant == null) {
            tenant = generateTenant();
        }
        EventClassFamily eventClassFamily;
        List<EventClassFamily> eventClassFamilies = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            eventClassFamily = new EventClassFamily();
            eventClassFamily.setTenant(tenant);
            eventClassFamily.setClassName("Test ClassName" + RANDOM.nextInt());
            eventClassFamily.setCreatedTime(new Date().getTime());
            eventClassFamily.setCreatedUsername("Test Username");
            eventClassFamily.setDescription("Test Description");
            eventClassFamily.setName("Test Name" + RANDOM.nextInt());
            eventClassFamily.setNamespace("Test Namespace");
            List<EventSchemaVersion> eventSchemaVersions = new ArrayList<>(eventSchemaVersionsCount);
            for (int j = 0; j < eventSchemaVersionsCount; j++) {
                EventSchemaVersion eventSchemaVersion = new EventSchemaVersion();
                eventSchemaVersion.setCreatedTime(new Date().getTime());
                eventSchemaVersion.setCreatedUsername("Test Username");
                eventSchemaVersion.setSchema("Test Schema" + RANDOM.nextInt());
                eventSchemaVersion.setVersion(1);
                eventSchemaVersions.add(eventSchemaVersion);
            }
            eventClassFamily.setSchemas(eventSchemaVersions);
            eventClassFamily = eventClassFamilyDao.save(eventClassFamily);
            Assert.assertNotNull(eventClassFamily);
            eventClassFamilies.add(eventClassFamily);
        }
        return eventClassFamilies;
    }

    protected List<EventClass> generateEventClass(Tenant tenant, EventClassFamily eventClassFamily, int count) {
        if (tenant == null) {
            tenant = generateTenant();
        }
        if (eventClassFamily == null) {
            eventClassFamily = generateEventClassFamily(tenant, 1).get(0);
        }

        EventClass eventClass;
        List<EventClass> eventClasses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            eventClass = new EventClass();
            eventClass.setTenant(tenant);
            eventClass.setEcf(eventClassFamily);
            eventClass.setFqn("Test FQN" + RANDOM.nextInt());
            eventClass.setSchema("Test Schema" + RANDOM.nextInt());
            eventClass.setType(EventClassType.EVENT);
            eventClass.setVersion(1);
            eventClass = eventClassDao.save(eventClass);
            Assert.assertNotNull(eventClass);
            eventClasses.add(eventClass);
        }
        return eventClasses;
    }

    protected List<ApplicationEventFamilyMap> generateApplicationEventFamilyMap(Tenant tenant, Application application,
                                                                                EventClassFamily eventClassFamily, int count, boolean generateApplicationEventMaps) {
        int applicationEventMapCount = 2;
        if (tenant == null) {
            tenant = generateTenant();
        }
        if (application == null) {
            application = generateApplication(tenant);
        }
        if (eventClassFamily == null) {
            eventClassFamily = generateEventClassFamily(tenant, 1).get(0);
        }

        ApplicationEventFamilyMap applicationEventFamilyMap;
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            applicationEventFamilyMap = new ApplicationEventFamilyMap();
            applicationEventFamilyMap.setApplication(application);
            applicationEventFamilyMap.setCreatedTime(new Date().getTime());
            applicationEventFamilyMap.setCreatedUsername("Test Username");
            applicationEventFamilyMap.setEcf(eventClassFamily);
            applicationEventFamilyMap.setVersion(1);
            if (generateApplicationEventMaps) {
                List<ApplicationEventMap> applicationEventMaps = new ArrayList<>(applicationEventMapCount);
                for (int j = 0; j < applicationEventMapCount; j++) {
                    ApplicationEventMap applicationEventMap = new ApplicationEventMap();
                    applicationEventMap.setAction(ApplicationEventAction.BOTH);
                    applicationEventMap.setFqn("Test FQN" + RANDOM.nextInt());
                    applicationEventMap.setEventClass(generateEventClass(tenant, eventClassFamily, 1).get(0));
                    applicationEventMaps.add(applicationEventMap);
                }
                applicationEventFamilyMap.setEventMaps(applicationEventMaps);
            }
            applicationEventFamilyMap = applicationEventFamilyMapDao.save(applicationEventFamilyMap);
            Assert.assertNotNull(applicationEventFamilyMap);
            applicationEventFamilyMaps.add(applicationEventFamilyMap);
        }

        return applicationEventFamilyMaps;
    }

    protected List<LogSchema> generateLogSchema(Tenant tenant, int ctlVersion, Application application, int count) {
        List<LogSchema> schemas = Collections.emptyList();
        try {
            if (application == null) {
                application = generateApplication(tenant);
            }
            CTLSchema ctlSchema = generateCTLSchema(DEFAULT_FQN, ctlVersion, application.getTenant(), null);
            LogSchema schema;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schema = new LogSchema();
                schema.setApplication(application);
                schema.setCtlSchema(ctlSchema);
                schema.setCreatedUsername("Test User");
                schema.setName("Test Name");
                schema = logSchemaDao.save(schema);
                Assert.assertNotNull(schema);
                schemas.add(schema);
            }
        } catch (Exception e) {
            LOG.error("Can't generate log schemas {}", e);
            Assert.fail("Can't generate log schemas.");
        }
        return schemas;
    }

    protected String readSchemaFileAsString(String filePath) throws IOException {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
            if (url != null) {
                Path path = Paths.get(url.toURI());
                byte[] bytes = Files.readAllBytes(path);
                return new String(bytes);
            }
        } catch (URISyntaxException e) {
            LOG.error("Can't generate configs {}", e);
        }
        return null;
    }

    protected UserVerifier generateUserVerifier(Application app, String verifierToken) {
        UserVerifier verifier = new UserVerifier();
        verifier.setName("GENERATED test Verifier");
        if (app == null) {
            app = generateApplication(null);
        }
        verifier.setApplication(app);
        if (verifierToken == null) {
            verifierToken = "token";
        }
        verifier.setVerifierToken(verifierToken);
        return verifierDao.save(verifier);
    }

    protected SdkProfile generateSdkProfile(Application application, String token) {
        SdkProfile entity = new SdkProfile();

        if (application == null) {
            application = this.generateApplication(null);
        }
        entity.setApplication(application);

        if (token == null) {
            token = "token";
        }
        entity.setToken(token);

        return sdkProfileDao.save(entity);
    }

}
