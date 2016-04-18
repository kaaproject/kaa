/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import java.util.Arrays;

public class UserConfigurationUpdate {

    private final String tenantId;
    private final String userId;
    private final String applicationToken;
    private final int schemaVersion;
    private final byte[] hash;

    public UserConfigurationUpdate(String tenantId, String userId, String applicationToken, int schemaVersion, byte[] hash) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.applicationToken = applicationToken;
        this.schemaVersion = schemaVersion;
        this.hash = hash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public byte[] getHash() {
        return hash;
    }

    public static UserConfigurationUpdate fromThrift(
            org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate notification) {
        return new UserConfigurationUpdate(notification.getTenantId(), notification.getUserId(), notification.getApplicationToken(),
                notification.getCfSchemaVersion(), notification.getUcfHash());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserConfigurationUpdate [tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append(", schemaVersion=");
        builder.append(schemaVersion);
        builder.append(", hash=");
        builder.append(Arrays.toString(hash));
        builder.append("]");
        return builder.toString();
    }
}
