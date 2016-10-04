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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.google.common.collect.Sets;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Defaults;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.client.CassandraClient;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CassandraEntityMapper<T> {

  private static final Map<Class<?>, CassandraEntityMapper<?>> mappers = new HashMap<>();
  private final List<String> nonKeyColumns = new ArrayList<>();
  private final List<String> keyColumns = new ArrayList<>();
  private final Map<String, PropertyDescriptor> fieldDescMap = new HashMap<>();
  private Map<String, Map<Class<?>, CassandraEntityMapper<?>>> udtMappers = new HashMap<>();

  private String name;

  /**
   * Create new instance of <code>CassandraEntityMapper</code>.
   *
   * @param entityClass is entity class
   * @param cassandraClient os cassandra client
   */
  public CassandraEntityMapper(Class<T> entityClass, CassandraClient cassandraClient) {

    Table tableAnnotation = entityClass.getAnnotation(Table.class);
    UDT udtAnnotation = entityClass.getAnnotation(UDT.class);
    this.name = tableAnnotation != null ? tableAnnotation.name() : udtAnnotation.name();

    for (Field field : entityClass.getDeclaredFields()) {
      if (field.isSynthetic()
          || (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
        continue;
      }

      if (field.getAnnotation(Transient.class) != null) {
        continue;
      }

      Column column = field.getAnnotation(Column.class);
      com.datastax.driver.mapping.annotations.Field fieldAnnotation
          = field.getAnnotation(com.datastax.driver.mapping.annotations.Field.class);
      if (column != null || fieldAnnotation != null) {
        if (column != null) {
          Class<? extends TypeCodec<?>> codecClass = column.codec();
          if (!codecClass.equals(Defaults.NoCodec.class)) {
            try {
              @SuppressWarnings("unchecked")
              TypeCodec<Object> instance = (TypeCodec<Object>) codecClass
                  .newInstance();

              cassandraClient.getSession()
                  .getCluster()
                  .getConfiguration()
                  .getCodecRegistry()
                  .register(instance);
            } catch (Exception exception) {
              throw new IllegalArgumentException(String.format(
                  "Cannot create an instance of custom codec %s for field %s",
                  codecClass, field
              ), exception);
            }
          }
        }
        String name = column != null ? column.name() : fieldAnnotation.name();
        if (field.isAnnotationPresent(PartitionKey.class)
            || field.isAnnotationPresent(ClusteringColumn.class)) {
          keyColumns.add(name);
        } else {
          nonKeyColumns.add(name);
        }
        String fieldName = field.getName();
        try {
          fieldDescMap.put(name, new PropertyDescriptor(
              fieldName, field.getDeclaringClass()));
        } catch (IntrospectionException exception) {
          throw new IllegalArgumentException(
              "Cannot find matching getter and setter for field '" + fieldName + "'");
        }

        Set<Class<?>> udts = findUdts(field.getGenericType());
        if (!udts.isEmpty()) {
          Map<Class<?>, CassandraEntityMapper<?>> udtMap = new HashMap<>();
          for (Class<?> udtClass : udts) {
            if (!udtMap.containsKey(udtClass)) {
              udtMap.put(
                  udtClass, getEntityMapperForClass(udtClass, cassandraClient));
            }
          }
          udtMappers.put(name, udtMap);
        }
      }
    }
  }

  /**
   * Factory method: found instance of <code>CassandraEntityMapper</code> from <code>mappers</code>
   * by <code>clazz</code> or create instance if not found and return it.
   *
   * @param clazz           using for searching instance of <code>CassandraEntityMapper</code> in
   *                        <code>mappers</code>
   * @param cassandraClient using when creating new instance of CassandraEntityMapper
   */
  @SuppressWarnings("unchecked")
  public static <E> CassandraEntityMapper<E> getEntityMapperForClass(
          Class<E> clazz,
          CassandraClient cassandraClient
  ) {
    CassandraEntityMapper<?> mapper = mappers.get(clazz);
    if (mapper == null) {
      if (clazz.isAnnotationPresent(Table.class)) {
        cassandraClient.getMapper(clazz);
      }
      mapper = new CassandraEntityMapper<E>(clazz, cassandraClient);
      mappers.put(clazz, mapper);
    }
    return (CassandraEntityMapper<E>) mapper;
  }

  static boolean mapsToCollection(Class<?> klass) {
    return mapsToList(klass) || mapsToSet(klass) || mapsToMap(klass);
  }

  private static boolean mapsToList(Class<?> klass) {
    return List.class.isAssignableFrom(klass);
  }

  private static boolean mapsToSet(Class<?> klass) {
    return Set.class.isAssignableFrom(klass);
  }

  private static boolean mapsToMap(Class<?> klass) {
    return Map.class.isAssignableFrom(klass);
  }

  static boolean isMappedUdt(Class<?> klass) {
    return klass.isAnnotationPresent(UDT.class);
  }

  static Set<Class<?>> findUdts(Type type) {
    Set<Class<?>> udts = findUdts(type, null);
    return (udts == null)
        ? Collections.<Class<?>>emptySet()
        : udts;
  }

  private static Set<Class<?>> findUdts(Type type, Set<Class<?>> udts) {
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      Type raw = pt.getRawType();
      if ((raw instanceof Class)) {
        Class<?> klass = (Class<?>) raw;
        if (mapsToCollection(klass)) {
          Type[] childTypes = pt.getActualTypeArguments();
          udts = findUdts(childTypes[0], udts);

          if (mapsToMap(klass)) {
            udts = findUdts(childTypes[1], udts);
          }
        }
      }
    } else if (type instanceof Class) {
      Class<?> klass = (Class<?>) type;
      if (isMappedUdt(klass)) {
        if (udts == null) {
          udts = Sets.newHashSet();
        }
        udts.add(klass);
      }
    }
    return udts;
  }

  public String getName() {
    return name;
  }

  public List<String> getNonKeyColumnNames() {
    return nonKeyColumns;
  }

  public List<String> getKeyColumnNames() {
    return keyColumns;
  }

  /**
   * Get column value for name, search value in field <code>fieldDescMap</code> or create new.
   *
   * @param name is name for which search column value
   * @param entity using for create column value
   * @param cassandraClient using for create column value
   * @return column value
   */
  public Object getColumnValueForName(String name,
                                      Object entity,
                                      CassandraClient cassandraClient) {
    PropertyDescriptor pd = fieldDescMap.get(name);
    try {
      Object value = pd.getReadMethod().invoke(entity);
      Map<Class<?>, CassandraEntityMapper<?>> udtMap = udtMappers.get(name);
      if (udtMap != null && value != null) {
        return convertValue(value, udtMap, cassandraClient);
      }
      return value;
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Could not get field '" + pd.getName() + "'");
    } catch (Exception execption) {
      throw new IllegalStateException(
          "Unable to access getter for '"
              + pd.getName() + "' in " + entity.getClass().getName(), execption);
    }
  }

  private Object convertValue(Object value,
                              Map<Class<?>, CassandraEntityMapper<?>> udtMap,
                              CassandraClient cassandraClient) {
    Class<?> valueClass = value.getClass();
    if (mapsToCollection(valueClass)) {
      if (mapsToList(valueClass)) {
        List<?> valList = (List<?>) value;
        List<Object> list = new ArrayList<>(valList.size());
        for (Object elem : valList) {
          list.add(convertValue(elem, udtMap, cassandraClient));
        }
        value = list;
      } else if (mapsToSet(valueClass)) {
        Set<?> valSet = (Set<?>) value;
        Set<Object> set = new HashSet<>(valSet.size());
        for (Object elem : valSet) {
          set.add(convertValue(elem, udtMap, cassandraClient));
        }
        value = set;
      } else if (mapsToMap(valueClass)) {
        Map<?, ?> valMap = (Map<?, ?>) value;
        Map<Object, Object> map = new HashMap<>(valMap.size());
        for (Object elem : map.keySet()) {
          Object elemVal = map.get(elem);
          map.put(
              convertValue(elem, udtMap, cassandraClient),
              convertValue(elemVal, udtMap, cassandraClient));
        }
        value = map;
      }
    } else if (udtMap.containsKey(valueClass)) {
      return convertValue(value, udtMap.get(valueClass), cassandraClient);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private UDTValue convertValue(Object value,
                                CassandraEntityMapper<?> mapper,
                                CassandraClient cassandraClient) {
    String keyspace = cassandraClient.getSession().getLoggedKeyspace();
    UserType userType = cassandraClient.getSession()
        .getCluster()
        .getMetadata()
        .getKeyspace(keyspace)
        .getUserType(mapper.getName());
    UDTValue udtValue = userType.newValue();
    for (String name : mapper.getNonKeyColumnNames()) {
      Object fieldValue = mapper.getColumnValueForName(name, value, cassandraClient);
      if (fieldValue != null) {
        udtValue.set(name, fieldValue, (Class<Object>) fieldValue.getClass());
      } else {
        udtValue.setToNull(name);
      }
    }
    return udtValue;
  }
}
