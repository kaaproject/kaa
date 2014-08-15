package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;

public class MongoAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = 82969770234945644L;
    private String collectionName;

    public MongoAppenderParametersDto() {
    }

    public MongoAppenderParametersDto(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collectionName == null) ? 0 : collectionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MongoAppenderParametersDto other = (MongoAppenderParametersDto) obj;
        if (collectionName == null) {
            if (other.collectionName != null)
                return false;
        } else if (!collectionName.equals(other.collectionName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MongoAppenderParametersDto [collectionName=" + collectionName + "]";
    }

}
