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

package org.kaaproject.kaa.common.dto.admin;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserDto extends org.kaaproject.kaa.common.dto.UserDto {

    private static final long serialVersionUID = 8016875668519720555L;

    @Size(min = 2 , max = 35)
    private String firstName;
    @Size(min = 2 , max = 35)
    private String lastName;
    @Size(min = 2 , max = 225)
    @NotNull(message="email can't be null")
    @Pattern(
            regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
            message = "email doesn't match regular expression pattern")
    private String mail;
    private String tempPassword;

    public UserDto() {
    }

    public UserDto(String externalUid,
            String username,
            String firstName,
            String lastName,
            String mail,
            KaaAuthorityDto authority) {
        setExternalUid(externalUid);
        setUsername(username);
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.setAuthority(authority);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((getExternalUid() == null) ? 0 : getExternalUid().hashCode());
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
        UserDto other = (UserDto) obj;
        if (getExternalUid() == null) {
            if (other.getExternalUid() != null) {
                return false;
            }
        } else if (!getExternalUid().equals(other.getExternalUid())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuthUserDto [firstName=" + firstName
                + ", lastName=" + lastName + ", mail=" + mail + ", toString()="
                + super.toString() + "]";
    }

}
