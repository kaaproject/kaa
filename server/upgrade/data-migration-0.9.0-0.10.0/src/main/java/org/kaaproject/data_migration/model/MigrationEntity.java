package org.kaaproject.data_migration.model;

import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

/**
 * Created by user482400 on 09.08.16.
 */
public class MigrationEntity {
    private CTLSchemaDto ctl;
    private BaseSchemaDto baseSchema;

    public MigrationEntity() {
    }

    public MigrationEntity(CTLSchemaDto ctl, BaseSchemaDto baseSchema) {
        this.ctl = ctl;
        this.baseSchema = baseSchema;
    }

    public CTLSchemaDto getCtl() {
        return ctl;
    }

    public void setCtl(CTLSchemaDto ctl) {
        this.ctl = ctl;
    }

    public BaseSchemaDto getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(BaseSchemaDto baseSchema) {
        this.baseSchema = baseSchema;
    }
}
