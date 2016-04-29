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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SendNotificationPlace extends TreePlace {

    private String applicationId;
    private String topicId;

    public SendNotificationPlace(String applicationId, String topicId) {
        this.applicationId = applicationId;
        this.topicId = topicId;
    }

    public String getApplicationId() {
        return applicationId;
    }
    
    public String getTopicId() {
        return topicId;
    }

    @Override
    public String getName() {
        return Utils.constants.sendNotification();
    }

    @Prefix(value = "sendNotif")
    public static class Tokenizer implements PlaceTokenizer<SendNotificationPlace>, PlaceConstants {

        @Override
        public SendNotificationPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new SendNotificationPlace(PlaceParams.getParam(APPLICATION_ID), PlaceParams.getParam(TOPIC_ID));
        }

        @Override
        public String getToken(SendNotificationPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(APPLICATION_ID, place.getApplicationId());
            PlaceParams.putParam(TOPIC_ID, place.getTopicId());
            return PlaceParams.generateToken();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SendNotificationPlace other = (SendNotificationPlace) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (topicId == null) {
            if (other.topicId != null) {
                return false;
            }
        } else if (!topicId.equals(other.topicId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new TopicPlace(applicationId, topicId);
    }

}
