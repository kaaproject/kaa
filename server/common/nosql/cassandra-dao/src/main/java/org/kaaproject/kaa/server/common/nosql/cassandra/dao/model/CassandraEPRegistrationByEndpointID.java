/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.server.common.dao.model.EndpointRegistration;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Table(name = CassandraModelConstants.EP_REGISTRATIONS_BY_ENDPOINT_ID_COLUMN_FAMILY_NAME)
public final class CassandraEPRegistrationByEndpointID implements Serializable {

    @Transient
    private static final long serialVersionUID = 1000L;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_REGISTRATION_BY_ENDPOINT_ID_ENDPOINT_ID_PROPERTY)
    private String endpointId;
    
    @Column(name = CassandraModelConstants.EP_REGISTRATION_BY_ENDPOINT_ID_CREDENTIALS_ID_PROPERTY)
    private String credentialsId;


    public static CassandraEPRegistrationByEndpointID fromEndpointRegistration(EndpointRegistration endpointRegistration) {
        String endpointId = endpointRegistration.getEndpointId();
        String credentialsId = endpointRegistration.getCredentialsId();
        return new CassandraEPRegistrationByEndpointID(endpointId, credentialsId);
    }

    public CassandraEPRegistrationByEndpointID() {
    }

    public CassandraEPRegistrationByEndpointID(String endpointId, String credentialsId) {
        this.endpointId = endpointId;
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getEndpointId() {
        return this.endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
