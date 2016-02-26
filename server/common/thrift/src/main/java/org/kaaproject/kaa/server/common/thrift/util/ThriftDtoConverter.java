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

package org.kaaproject.kaa.server.common.thrift.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kaaproject.kaa.common.dto.DtoByteMarshaller;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct;

/**
 * The Class ThriftDtoConverter.<br>
 * Used to convert Data Transfer Objects (DTOs) to Thrift DataStruct structure
 * and vice versa
 */
public class ThriftDtoConverter {

    private ThriftDtoConverter() {
    }

    /**
     * Convert DTO to thrift DataStruct.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dto
     *            the DTO
     * @return the thrift DataStruct
     */
    public static <T extends HasId> DataStruct toDataStruct(T dto) {
        DataStruct dataStruct = new DataStruct();
        if (dto != null) {
            dataStruct.setKey(dto.getId());
            dataStruct.setData(DtoByteMarshaller.toBytes(dto));
        }
        return dataStruct;
    }
    
    /**
     * Convert DTO to thrift DataStruct.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dto
     *            the DTO
     * @return the thrift DataStruct
     */
    public static <T> DataStruct toGenericDataStruct(T dto) {
        DataStruct dataStruct = new DataStruct();
        if (dto != null) {
            dataStruct.setData(DtoByteMarshaller.toBytes(dto));
        }
        return dataStruct;
    }

    /**
     * Convert DTOs list to thrift DataStructs list.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dtoList
     *            the DTOs list
     * @return the thrift DataStructs list
     */
    public static <T extends HasId> List<DataStruct> toDataStructList(
            Collection<T> dtoList) {
        List<DataStruct> dataStructList = new ArrayList<DataStruct>(
                dtoList.size());
        for (T dto : dtoList) {
            dataStructList.add(toDataStruct(dto));
        }
        return dataStructList;
    }
    
    /**
     * Convert DTOs list to thrift DataStructs list.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dtoList
     *            the DTOs list
     * @return the thrift DataStructs list
     */
    public static <T> List<DataStruct> toGenericDataStructList(
            Collection<T> dtoList) {
        List<DataStruct> dataStructList = new ArrayList<DataStruct>(
                dtoList.size());
        for (T dto : dtoList) {
            dataStructList.add(toGenericDataStruct(dto));
        }
        return dataStructList;
    }

    /**
     * Convert thrift DataStruct to DTO.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dataStruct
     *            the thrift DataStruct
     * @return the DTO
     */
    public static <T extends HasId> T toDto(DataStruct dataStruct) {
        T dto = null;
        if (dataStruct != null && dataStruct.getData() != null) {
            dto = DtoByteMarshaller.fromBytes(dataStruct.getData());
        }
        return dto;
    }

    /**
     * Convert thrift DataStruct to DTO.
     *
     * @param <T>
     *            the DTO generic type
     * @param dataStruct
     *            the thrift DataStruct
     * @return the DTO
     */
    public static <T> T toGenericDto(DataStruct dataStruct) {
        T dto = null;
        if (dataStruct != null && dataStruct.getData() != null) {
            dto = DtoByteMarshaller.fromBytes(dataStruct.getData());
        }
        return dto;
    }

    /**
     * Convert thrift DataStructs list to DTOs list.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dataStructList
     *            the thrift DataStructs list
     * @return the DTOs list
     */
    public static <T extends HasId> List<T> toDtoList(
            Collection<DataStruct> dataStructList) {
        List<T> dtoList = new ArrayList<T>(dataStructList.size());
        for (DataStruct dataStruct : dataStructList) {
            dtoList.add(ThriftDtoConverter.<T> toDto(dataStruct));
        }
        return dtoList;
    }
    
    /**
     * Convert thrift DataStructs list to DTOs list.
     * 
     * @param <T>
     *            the DTO generic type
     * @param dataStructList
     *            the thrift DataStructs list
     * @return the DTOs list
     */
    public static <T> List<T> toGenericDtoList(
            Collection<DataStruct> dataStructList) {
        List<T> dtoList = new ArrayList<T>(dataStructList.size());
        for (DataStruct dataStruct : dataStructList) {
            dtoList.add(ThriftDtoConverter.<T> toGenericDto(dataStruct));
        }
        return dtoList;
    }

}
