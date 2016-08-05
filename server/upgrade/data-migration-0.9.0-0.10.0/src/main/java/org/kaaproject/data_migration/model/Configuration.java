package org.kaaproject.data_migration.model;

import java.io.Serializable;

/**
 * Created by user482400 on 04.08.16.
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = -1176562073;

    private byte[] configuration_body;
    private Integer configuration_schems_version;
    private Long    id;
    private Long configuration_schems_id;

    public Configuration() {}

    public Configuration(Configuration value) {
        this.configuration_body = value.configuration_body;
        this.configuration_schems_version = value.configuration_schems_version;
        this.id = value.id;
        this.configuration_schems_id = value.configuration_schems_id;
    }

    public Configuration(
            byte[] configurationBody,
            Integer configurationSchemsVersion,
            Long id,
            Long configurationSchemsId
    ) {
        this.configuration_body = configurationBody;
        this.configuration_schems_version = configurationSchemsVersion;
        this.id = id;
        this.configuration_schems_id = configurationSchemsId;
    }

    public byte[] getConfiguration_body() {
        return this.configuration_body;
    }

    public void setConfiguration_body(byte[] configuration_body) {
        this.configuration_body = configuration_body;
    }

    public Integer getConfiguration_schems_version() {
        return this.configuration_schems_version;
    }

    public void setConfiguration_schems_version(Integer configuration_schems_version) {
        this.configuration_schems_version = configuration_schems_version;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfiguration_schems_id() {
        return this.configuration_schems_id;
    }

    public void setConfiguration_schems_id(Long configuration_schems_id) {
        this.configuration_schems_id = configuration_schems_id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("configuration.Configuration[");

        sb.append("configuration_body=").append(new String(configuration_body));
        sb.append(", configuration_schems_version=").append(configuration_schems_version);
        sb.append(", id=").append(id);
        sb.append(", configuration_schems_id=").append(configuration_schems_id);

        sb.append("]");
        return sb.toString();
    }
}
