package org.kaaproject.kaa.common.dto.plugin;


import java.io.Serializable;

public class ContractMessageDto implements Serializable {

    private static final long serialVersionUID = 5734378810574411385L;

    private String id;
    private String fqn;
    private Integer version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractMessageDto)) {
            return false;
        }

        ContractMessageDto that = (ContractMessageDto) o;

        if (fqn != null ? !fqn.equals(that.fqn) : that.fqn != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContractMessageDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", fqn='").append(fqn).append('\'');
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }
}
