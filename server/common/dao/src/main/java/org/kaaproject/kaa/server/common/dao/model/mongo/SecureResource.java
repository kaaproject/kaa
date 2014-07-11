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

package org.kaaproject.kaa.server.common.dao.model.mongo;

import java.io.Serializable;
import org.kaaproject.kaa.common.dto.logs.security.MongoResourceDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class SecureResource  implements ToDto<MongoResourceDto>, Serializable {
    
    private static final long serialVersionUID = -1535511974238826498L;
    
    @Indexed
    private String db;
    private String collection;

    public SecureResource() {
        
    }

    public SecureResource(MongoResourceDto dto) {
        this.db = dto.getDB();
        this.collection = dto.getCollection();
    }
    
    public String getDB() {
        return db;
    }

    public void setDB(String db) {
        this.db = db;
    }
    
    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SecureResource that = (SecureResource) o;
        
        if (db != null ? !db.equals(that.db) : that.db != null) {
            return false;
        }
        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = db != null ? db.hashCode() : 0;
        result = 31 * result + (collection != null ? collection.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogSchema{" +
                "db='" + db + '\'' +
                "collection='" + collection + '\'' +
                '}';
    }

    @Override
    public MongoResourceDto toDto() {
        MongoResourceDto dto = new MongoResourceDto();
        dto.setDB(db);
        dto.setCollection(collection);
        return dto;
    }

}
