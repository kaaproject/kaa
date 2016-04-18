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

package org.kaaproject.kaa.common.dto.user;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;

public class UserVerifierDto extends PluginDto {

    private static final long serialVersionUID = -6910185820548884149L;

    private String verifierToken;

    public UserVerifierDto() {
        super();
    }

    public UserVerifierDto(UserVerifierDto userVerifierDto) {
        super(userVerifierDto);
        this.verifierToken = userVerifierDto.verifierToken;
    }

    public String getVerifierToken() {
        return verifierToken;
    }

    public void setVerifierToken(String verifierToken) {
        this.verifierToken = verifierToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((verifierToken == null) ? 0 : verifierToken.hashCode());
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
        UserVerifierDto other = (UserVerifierDto) obj;
        if (verifierToken == null) {
            if (other.verifierToken != null) {
                return false;
            }
        } else if (!verifierToken.equals(other.verifierToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserVerifierDto [verifierToken=");
        builder.append(verifierToken);
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}