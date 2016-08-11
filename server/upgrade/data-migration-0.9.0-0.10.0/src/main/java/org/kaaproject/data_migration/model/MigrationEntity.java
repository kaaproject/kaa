package org.kaaproject.data_migration.model;

import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;


public class MigrationEntity {
    private Ctl ctl;
    private Schema schema;

    public MigrationEntity(Ctl ctl, Schema schema) {
        this.ctl = ctl;
        this.schema = schema;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Ctl getCtl() {
        return ctl;
    }

    public void setCtl(Ctl ctl) {
        this.ctl = ctl;
    }
}
