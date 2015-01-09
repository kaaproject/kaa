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

package org.kaaproject.kaa.server.admin.services.dao;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.admin.services.entity.Properties;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository("propertiesFacade")
@Transactional
public class PropertiesFacade {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesFacade.class);

    private static final String SCHEMA = "SCHEMA$";

    @PersistenceContext(unitName = "kaaSec")
    private EntityManager em;

    public <S extends SpecificRecordBase> S getSpecificProperties(Class<S> propertiesClass) {
        Properties entity = findOrCreateByClass(propertiesClass);
        S specificProperties = null;
        if (entity != null) {
            AvroByteArrayConverter<S> converter =
                    new AvroByteArrayConverter<>(propertiesClass);
            try {
                specificProperties = converter.fromByteArray(entity.getRawConfiguration());
            } catch (IOException e) {
                LOG.error("Unable to parse raw data for specific record " + propertiesClass.getSimpleName(), e);
            }
        } 
        if (specificProperties == null) {
            specificProperties = buildDefaultProperties(propertiesClass);
        }
        return specificProperties;
    }
    
    public <S extends SpecificRecordBase> PropertiesDto getPropertiesDto(Class<S> propertiesClass) throws Exception {
        Properties entity = findOrCreateByClass(propertiesClass);
        return toDto(entity, propertiesClass);
    }
    
    public <S extends SpecificRecordBase> PropertiesDto editPropertiesDto(PropertiesDto propertiesDto, Class<S> propertiesClass) throws Exception {
        Properties entity = findOrCreateByClass(propertiesClass);
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(propertiesDto.getConfiguration());
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
        byte[] rawConfiguration = converter.encode(record);
        entity.setRawConfiguration(rawConfiguration);
        save(entity);
        return toDto(entity, propertiesClass);
    }
    
    private Long save(Properties properties) {
        em.persist(properties);
        return properties.getId();
    }
    
    private <S extends SpecificRecordBase> Properties findOrCreateByClass(Class<S> propertiesClass) {
        TypedQuery<Properties> query = em.createQuery("SELECT p FROM Properties p WHERE p.fqn = :fqn", Properties.class);
        query.setParameter("fqn", propertiesClass.getName());
        List<Properties> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            return createDefault(propertiesClass);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <S extends SpecificRecordBase> S buildDefaultProperties(Class<S> propertiesClass) {
        S result = null;
        try {
            Method newBuilderMethod = propertiesClass.getMethod("newBuilder");
            Object builderObject = newBuilderMethod.invoke(null);
            Method buildMethod = builderObject.getClass().getMethod("build");
            result = (S)buildMethod.invoke(builderObject);
        } catch (Exception e) {
            LOG.error("Unable to build default specific properties for class " + propertiesClass.getSimpleName(), e);
        }
        return result;
    }
    
    private <S extends SpecificRecordBase> Properties createDefault(Class<S> propertiesClass) {
        Properties properties = new Properties();
        
        S specificProperties = buildDefaultProperties(propertiesClass);
        AvroByteArrayConverter<S> converter =
                new AvroByteArrayConverter<>(propertiesClass);
        try {
            properties.setRawConfiguration(converter.toByteArray(specificProperties));
        } catch (IOException e) {
            LOG.error("Unable to serialize configuration for properties", e);
        }
        properties.setFqn(propertiesClass.getName());
        Long id = save(properties);
        properties.setId(id);
        return properties;
    }
    
    private <S extends SpecificRecordBase> PropertiesDto toDto(Properties entity, Class<S> propertiesClass) throws Exception {
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setId(String.valueOf(entity.getId()));
        Schema schema = (Schema)propertiesClass.getField(SCHEMA).get(null);
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
        GenericRecord record = converter.decodeBinary(entity.getRawConfiguration());
        RecordField configuration = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        propertiesDto.setConfiguration(configuration);
        return propertiesDto;
    }
}
