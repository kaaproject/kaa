package org.kaaproject.data_migration.model;


public class Ctl {
    private final Long id;
    private final CtlMetaInfo metaInfo;

    public Ctl(Long id, CtlMetaInfo metaInfo) {
        this.id = id;
        this.metaInfo = metaInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ctl ctl = (Ctl) o;

        if (id != null ? !id.equals(ctl.id) : ctl.id != null) return false;
        return metaInfo.equals(ctl.metaInfo);

    }

    @Override
    public int hashCode() {
        return metaInfo.hashCode();
    }

    public Long getId() {
        return id;
    }

    public CtlMetaInfo getMetaInfo() {
        return metaInfo;
    }
}
