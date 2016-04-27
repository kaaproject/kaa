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

package org.kaaproject.kaa.server.common.dao.model.sql;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.kaaproject.kaa.server.common.dao.model.ToDto;

@MappedSuperclass
public abstract class GenericModel<T> implements Serializable, ToDto<T> {

    private static final long serialVersionUID = 8371621337499494435L;

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE)
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStringId() {
        return id != null ? id.toString() : null;
    }

    public GenericModel<T> newInstance(String id) {
        return newInstance(ModelUtils.getLongId(id));
    }

    protected abstract T createDto();

    protected abstract GenericModel<T> newInstance(Long id);
}
