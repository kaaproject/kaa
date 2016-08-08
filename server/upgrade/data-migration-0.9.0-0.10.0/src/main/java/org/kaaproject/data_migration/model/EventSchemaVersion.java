package org.kaaproject.data_migration.model;

public class EventSchemaVersion {
    private Long    id;
    private String schems;

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

    @Override
    public String toString() {
        return "EventSchemaVersion{" +
                "id=" + id +
                ", schems=" + schems +
                '}';
    }
}
