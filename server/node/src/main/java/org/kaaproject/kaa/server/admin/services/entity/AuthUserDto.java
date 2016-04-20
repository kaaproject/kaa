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

package org.kaaproject.kaa.server.admin.services.entity;

import java.util.ArrayList;
import java.util.Collection;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserDto extends UserDto implements UserDetails {

    private static final long serialVersionUID = 8016875668519720555L;

    private String password;
    private boolean tempPassword;
    private boolean enabled;

    private Collection<GrantedAuthority> authorities;

    public AuthUserDto() {
    }

    public AuthUserDto(User user) {
        setExternalUid(user.getId().toString());
        setUsername(user.getUsername());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setMail(user.getMail());
        this.password = user.getPassword();
        this.setAuthority(KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
        this.tempPassword = user.isTempPassword();
        this.enabled = user.isEnabled();
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(boolean tempPassword) {
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
        AuthUserDto other = (AuthUserDto) obj;
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
        return "AuthUserDto [password=" + password
                + ", tempPassword=" + tempPassword + ", toString()="
                + super.toString() + "]";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<>();
            GrantedAuthority authority = new GrantedAuthority() {

                private static final long serialVersionUID = 3750701580428140468L;

                @Override
                public String getAuthority() {
                    return AuthUserDto.this.getAuthority().name();
                }
            };
            authorities.add(authority);
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
