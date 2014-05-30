/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = ProfileFilter.COLLECTION_NAME)
public final class ProfileFilter extends AbstractStructure<ProfileFilterDto> {

    private static final long serialVersionUID = -8852525123277654406L;

    public static final String COLLECTION_NAME = "profile_filter";

    public ProfileFilter() {
    }
    
    public ProfileFilter(ProfileFilterDto dto) {
        super(dto);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ProfileFilter) {
            ProfileFilter that = (ProfileFilter) other;
            return super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "ProfileFilter{" + super.toString() +
                '}';
    }


    @Override
    public ProfileFilterDto toDto() {
        return super.toDto();
    }

    @Override
    protected ProfileFilterDto createDto() {
        return new ProfileFilterDto();
    }
    
}
