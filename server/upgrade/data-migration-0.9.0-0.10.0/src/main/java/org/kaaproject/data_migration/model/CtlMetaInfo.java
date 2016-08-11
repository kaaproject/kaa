package org.kaaproject.data_migration.model;

public class CtlMetaInfo {

    private final String fqn;
    private final Long appId;
    private final Long tenatnId;

    public CtlMetaInfo(String fqn, Long appId, Long tenatnId) {
        this.fqn = fqn;
        this.appId = appId;
        this.tenatnId = tenatnId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CtlMetaInfo that = (CtlMetaInfo) o;

        if (!fqn.equals(that.fqn)) return false;
        return appId != null ? appId.equals(that.appId) : that.appId == null;

    }

    @Override
    public int hashCode() {
        int result = fqn.hashCode();
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        return result;
    }

    public Long getAppId() {
        return appId;
    }

    public Long getTenatnId() {
        return tenatnId;
    }

    public String getFqn() {
        return fqn;
    }
}
