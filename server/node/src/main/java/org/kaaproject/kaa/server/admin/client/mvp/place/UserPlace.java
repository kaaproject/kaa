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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class UserPlace extends TreePlace {

    private String userId;
    private String tenId;
    private String userName;

    public UserPlace(String userId) {
       this(userId,null);
    }
    public UserPlace(String userId,String tenId) {
        this.userId = userId;
        this.tenId=tenId;
    }


    public void setUserName(String name) {
        this.userName = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenId() {
        return tenId;
    }

    @Prefix(value = "usr")
    public static class Tokenizer implements PlaceTokenizer<UserPlace>, PlaceConstants {

        @Override
        public UserPlace getPlace(String token) {
            PlaceParams.paramsFromToken(token);
            return new UserPlace(PlaceParams.getParam(USER_ID),PlaceParams.getParam(TENANT_ID));
        }

        @Override
        public String getToken(UserPlace place) {
            PlaceParams.clear();
            PlaceParams.putParam(USER_ID, place.getUserId());
            PlaceParams.putParam(TENANT_ID, place.getTenId());
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
        UserPlace other = (UserPlace) obj;
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new UsersPlace();
    }

}
