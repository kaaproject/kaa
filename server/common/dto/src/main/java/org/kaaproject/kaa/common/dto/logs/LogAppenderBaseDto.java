package org.kaaproject.kaa.common.dto.logs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.AbstractDetailDto;
import org.kaaproject.kaa.common.dto.HasId;

public abstract class LogAppenderBaseDto extends AbstractDetailDto implements HasId, Serializable {
	
	private static final long serialVersionUID = 8035147059931231619L;
	
    private String id;
    private String applicationId;
    private String applicationToken;
    private String tenantId;
    private int minLogSchemaVersion;
    private int maxLogSchemaVersion;
    private boolean confirmDelivery = true;
    private LogAppenderStatusDto status;
    private String typeName;
    private String appenderClassName;
    private List<LogHeaderStructureDto> headerStructure;

    public LogAppenderBaseDto() {
        super();
    }
    
    public LogAppenderBaseDto(LogAppenderBaseDto logAppenderDto) {
        super(logAppenderDto);
        this.id = logAppenderDto.getId();
        this.applicationId = logAppenderDto.getApplicationId();
        this.applicationToken = logAppenderDto.getApplicationToken();
        this.tenantId = logAppenderDto.getTenantId();
        this.minLogSchemaVersion = logAppenderDto.getMinLogSchemaVersion();
        this.maxLogSchemaVersion = logAppenderDto.getMaxLogSchemaVersion();
        this.confirmDelivery = logAppenderDto.isConfirmDelivery();
        this.status = logAppenderDto.getStatus();
        this.typeName = logAppenderDto.getTypeName();
        this.appenderClassName = logAppenderDto.getAppenderClassName();
        this.headerStructure = new ArrayList<LogHeaderStructureDto>(logAppenderDto.getHeaderStructure());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public int getMinLogSchemaVersion() {
        return minLogSchemaVersion;
    }

    public void setMinLogSchemaVersion(int minLogSchemaVersion) {
        this.minLogSchemaVersion = minLogSchemaVersion;
    }

    public int getMaxLogSchemaVersion() {
        return maxLogSchemaVersion;
    }

    public void setMaxLogSchemaVersion(int maxLogSchemaVersion) {
        this.maxLogSchemaVersion = maxLogSchemaVersion;
    }

    public LogAppenderStatusDto getStatus() {
        return status;
    }

    public void setStatus(LogAppenderStatusDto status) {
        this.status = status;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getAppenderClassName() {
        return appenderClassName;
    }

    public void setAppenderClassName(String appenderClassName) {
        this.appenderClassName = appenderClassName;
    }
    
    public List<LogHeaderStructureDto> getHeaderStructure() {
        return headerStructure;
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        this.headerStructure = headerStructure;
    }

    public boolean isConfirmDelivery() {
		return confirmDelivery;
	}

	public void setConfirmDelivery(boolean confirmDelivery) {
		this.confirmDelivery = confirmDelivery;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((appenderClassName == null) ? 0 : appenderClassName
                        .hashCode());
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result
                + ((headerStructure == null) ? 0 : headerStructure.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + maxLogSchemaVersion;
        result = prime * result + minLogSchemaVersion;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result
                + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogAppenderBaseDto other = (LogAppenderBaseDto) obj;
        if (appenderClassName == null) {
            if (other.appenderClassName != null) {
                return false;
            }
        } else if (!appenderClassName.equals(other.appenderClassName)) {
            return false;
        }
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (headerStructure == null) {
            if (other.headerStructure != null) {
                return false;
            }
        } else if (!headerStructure.equals(other.headerStructure)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (maxLogSchemaVersion != other.maxLogSchemaVersion) {
            return false;
        }
        if (minLogSchemaVersion != other.minLogSchemaVersion) {
            return false;
        }
        if (confirmDelivery != other.confirmDelivery) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        if (typeName == null) {
            if (other.typeName != null) {
                return false;
            }
        } else if (!typeName.equals(other.typeName)) {
            return false;
        }
        return true;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LogAppenderDto [id=");
		builder.append(id);
		builder.append(", applicationId=");
		builder.append(applicationId);
		builder.append(", applicationToken=");
		builder.append(applicationToken);
		builder.append(", tenantId=");
		builder.append(tenantId);
		builder.append(", minLogSchemaVersion=");
		builder.append(minLogSchemaVersion);
		builder.append(", maxLogSchemaVersion=");
		builder.append(maxLogSchemaVersion);
		builder.append(", confirmDelivery=");
		builder.append(confirmDelivery);
		builder.append(", status=");
		builder.append(status);
		builder.append(", typeName=");
		builder.append(typeName);
		builder.append(", appenderClassName=");
		builder.append(appenderClassName);
		builder.append(", headerStructure=");
		builder.append(headerStructure);
		builder.append("]");
		return builder.toString();
	}
}
