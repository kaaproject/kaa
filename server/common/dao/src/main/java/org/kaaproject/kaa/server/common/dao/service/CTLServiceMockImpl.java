package org.kaaproject.kaa.server.common.dao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.kaaproject.kaa.common.dto.ctl.CTLDependencyDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScope;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CTLServiceMockImpl implements CTLService {

    public static List<CTLSchemaDto> storage = new ArrayList<>();

    @Override
    public CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) {
        schema.setId(UUID.randomUUID().toString());
        storage.add(schema);
        return schema;
    }

    @Override
    public void removeCTLSchemaById(String id) {
        for (CTLSchemaDto object : storage) {
            if (id.equals(object.getId())) {
                storage.remove(object);
                break;
            }
        }
    }

    @Override
    public void removeCTLSchemaByFqnAndVersionAndTenantId(String fqn, int version, String tenantId) {
        for (CTLSchemaDto object : storage) {
            if (fqn.equals(object.getFqn()) && version == object.getVersion() && tenantId.equals(object.getTenantId())) {
                storage.remove(object);
                break;
            }
        }
    }

    @Override
    public CTLSchemaDto findCTLSchemaById(String id) {
        CTLSchemaDto found = null;
        for (CTLSchemaDto object : storage) {
            if (id.equals(object.getId())) {
                found = object;
                break;
            }
        }
        return found;
    }

    @Override
    public CTLSchemaDto findCTLSchemaByFqnAndVersionAndTenantId(String fqn, int version, String tenantId) {
        CTLSchemaDto found = null;
        for (CTLSchemaDto object : storage) {
            if (fqn.equals(object.getFqn()) && version == object.getVersion()
                    && (tenantId.equals(object.getTenantId()) || object.getScope() == CTLSchemaScope.SYSTEM)) {
                found = object;
                break;
            }
        }
        return found;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemas() {
        return storage;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId) {
        List<CTLSchemaDto> found = new ArrayList<>();
        for (CTLSchemaDto object : storage) {
            if (tenantId.equals(object.getTenantId())) {
                found.add(object);
            }
        }
        return found;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByApplicationId(String applicationId) {
        List<CTLSchemaDto> found = new ArrayList<>();
        for (CTLSchemaDto object : storage) {
            if (applicationId.equals(object.getApplicationId())) {
                found.add(object);
            }
        }
        return found;
    }

    @Override
    public List<CTLSchemaDto> findSystemCTLSchemas() {
        List<CTLSchemaDto> found = new ArrayList<>();
        for (CTLSchemaDto object : storage) {
            if (object.getScope() == CTLSchemaScope.SYSTEM) {
                found.add(object);
            }
        }
        return found;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String schemaId) {
        return null;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String fqn, int version, String tenantId) {
        List<CTLSchemaDto> found = new ArrayList<>();
        for (CTLSchemaDto object : storage) {
            if (object.getDependencies() != null) {
                for (CTLDependencyDto dependency : object.getDependencies()) {
                    // TODO: Check for tenant identifier
                    if (fqn.equals(dependency.getFqn()) && version == dependency.getVersion()) {
                        found.add(object);
                        break;
                    }
                }
            }
        }
        return found;
    }
}
