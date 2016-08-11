package org.kaaproject.data_migration.model;


import com.sun.istack.internal.NotNull;

public class Ctl {
    private final Long id;
    private final CtlMetaInfo metaInfo;
    private final String defaultRecord;

    public Ctl(Long id, CtlMetaInfo metaInfo, String defaultRecord) {
        this.id = id;
        this.metaInfo = metaInfo;
        this.defaultRecord = defaultRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ctl ctl = (Ctl) o;

        if (!metaInfo.equals(ctl.metaInfo)) return false;
        return defaultRecord.equals(ctl.defaultRecord);

    }

    @Override
    public int hashCode() {
        int result = metaInfo.hashCode();
        result = 31 * result + defaultRecord.hashCode();
        return result;
    }

    public Long getId() {
        return id;
    }

    public CtlMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public String getDefaultRecord() {
        return defaultRecord;
    }
}
