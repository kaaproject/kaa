package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_CLASS_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_RAW_CONFIGURATION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.USER_VERIFIER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;

@Entity
@Table(name = USER_VERIFIER_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public class UserVerifier extends GenericModel<UserVerifierDto> implements Serializable {

    private static final long serialVersionUID = -6822520685170109625L;
    
    @Column(name = USER_VERIFIER_NAME)
    private String name;
    
    @Column(name = USER_VERIFIER_ID)
    private int verifierId;
    
    @ManyToOne
    @JoinColumn(name = USER_VERIFIER_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;
    
    @Column(name = USER_VERIFIER_DESCRIPTION, length = 1000)
    private String description;

    @Column(name = USER_VERIFIER_CREATED_USERNAME)
    private String createdUsername;

    @Column(name = USER_VERIFIER_CREATED_TIME)
    private long createdTime;
    
    @Column(name = USER_VERIFIER_CLASS_NAME)
    private String className;
    
    @Lob
    @Column(name = USER_VERIFIER_RAW_CONFIGURATION)
    private byte[] rawConfiguration;

    public UserVerifier() {
        super();
    }

    public UserVerifier(UserVerifierDto dto) {
        if (dto != null) {
            this.id = getLongId(dto.getId());
            this.verifierId = dto.getVerifierId();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            this.name = dto.getName();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            this.className = dto.getClassName();
            this.rawConfiguration = dto.getRawConfiguration();
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(int verifierId) {
        this.verifierId = verifierId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
    }

    @Override
    public UserVerifierDto toDto() {
        UserVerifierDto dto = createDto();
        dto.setId(getStringId());
        dto.setVerifierId(verifierId);
        dto.setApplicationId(application.getStringId());
        dto.setName(name);
        dto.setCreatedTime(createdTime);
        dto.setCreatedUsername(createdUsername);
        dto.setDescription(description);
        dto.setClassName(className);
        dto.setRawConfiguration(rawConfiguration);
        return dto;
    }

    @Override
    protected UserVerifierDto createDto() {
        return new UserVerifierDto();
    }
}
