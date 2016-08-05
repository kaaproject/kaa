package org.kaaproject.data_migration.model;

import java.util.Arrays;

/**
 * Created by user482400 on 04.08.16.
 */
public class EventSchemaVersion {
    private Long    id;
    private byte[] schems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getSchems() {
        return schems;
    }

    public void setSchems(byte[] schems) {
        this.schems = schems;
    }

    @Override
    public String toString() {
        return "EventSchemaVersion{" +
                "id=" + id +
                ", schems=" + Arrays.toString(schems) +
                '}';
    }
}
