package org.kaaproject.data_migration.model;

/**
 * Created by user482400 on 04.08.16.
 */
public class EventClass {
    private Long    id;
    private String  schems;
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchems() {
        return schems;
    }

    public void setSchems(String schems) {
        this.schems = schems;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
