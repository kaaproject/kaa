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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class UpdateNotificationDto<T> implements Serializable {

    private static final long serialVersionUID = 2444947324481675806L;

    private String appId;
    private int appSeqNumber;
    private String groupId;
    private int groupSeqNumber;
    private String topicId;
    private T payload;
    private ChangeType changeType;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getAppSeqNumber() {
        return appSeqNumber;
    }

    public void setAppSeqNumber(int appSeqNumber) {
        this.appSeqNumber = appSeqNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getGroupSeqNumber() {
        return groupSeqNumber;
    }

    public void setGroupSeqNumber(int groupSeqNumber) {
        this.groupSeqNumber = groupSeqNumber;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }
}
