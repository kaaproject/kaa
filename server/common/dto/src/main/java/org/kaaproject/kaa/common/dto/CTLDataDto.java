package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class CTLDataDto implements Serializable {

    private static final long serialVersionUID = -9107671325547868060L;

    private String ctlSchemaId;
    private String body;

    public CTLDataDto() {
    }

    public CTLDataDto(String ctlSchemaId, String body) {
        this.ctlSchemaId = ctlSchemaId;
        this.body = body;
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLDataDto that = (CTLDataDto) o;

        if (ctlSchemaId != null ? !ctlSchemaId.equals(that.ctlSchemaId) : that.ctlSchemaId != null) return false;
        return body != null ? body.equals(that.body) : that.body == null;

    }

    @Override
    public int hashCode() {
        int result = ctlSchemaId != null ? ctlSchemaId.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLDataDto{" +
                "ctlSchemaId='" + ctlSchemaId + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
