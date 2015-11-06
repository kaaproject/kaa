package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;

import java.io.Serializable;

public class CTLSchema extends GenericModel implements Serializable {

    private String scope;
    private Tenant tenant;
    private Application application;
    private Integer version;
    private String body;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
