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
package org.kaaproject.kaa.server.common.zk.gen;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class OperationsNodeInfo extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"OperationsNodeInfo\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"connectionInfo\",\"type\":{\"type\":\"record\",\"name\":\"ConnectionInfo\",\"fields\":[{\"name\":\"thriftHost\",\"type\":\"string\"},{\"name\":\"thriftPort\",\"type\":\"int\"},{\"name\":\"publicKey\",\"type\":[\"bytes\",\"null\"]}]}},{\"name\":\"timeStarted\",\"type\":\"long\"},{\"name\":\"SupportedChannelsArray\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"SupportedChannel\",\"fields\":[{\"name\":\"ZkChannel\",\"type\":{\"type\":\"record\",\"name\":\"ZkSupportedChannel\",\"fields\":[{\"name\":\"ChannelType\",\"type\":{\"type\":\"enum\",\"name\":\"ZkChannelType\",\"symbols\":[\"HTTP\",\"HTTP_LP\"]}},{\"name\":\"RedirectionSupported\",\"type\":\"boolean\"},{\"name\":\"CommunicationParameters\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":\"IpComunicationParameters\"}]}]},{\"name\":\"ChannelStatistics\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":{\"type\":\"record\",\"name\":\"BaseStatistics\",\"fields\":[{\"name\":\"processedRequestCount\",\"type\":\"int\"},{\"name\":\"registeredUsersCount\",\"type\":\"int\"},{\"name\":\"deltaCalculationCount\",\"type\":\"int\"},{\"name\":\"timeStarted\",\"type\":\"long\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":\"BaseStatistics\"}]}]}]}}]}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo;
   private long timeStarted;
   private java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> SupportedChannelsArray;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public OperationsNodeInfo() {}

  /**
   * All-args constructor.
   */
  public OperationsNodeInfo(org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo, java.lang.Long timeStarted, java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> SupportedChannelsArray) {
    this.connectionInfo = connectionInfo;
    this.timeStarted = timeStarted;
    this.SupportedChannelsArray = SupportedChannelsArray;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return connectionInfo;
    case 1: return timeStarted;
    case 2: return SupportedChannelsArray;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: connectionInfo = (org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo)value$; break;
    case 1: timeStarted = (java.lang.Long)value$; break;
    case 2: SupportedChannelsArray = (java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'connectionInfo' field.
   */
  public org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  /**
   * Sets the value of the 'connectionInfo' field.
   * @param value the value to set.
   */
  public void setConnectionInfo(org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo value) {
    this.connectionInfo = value;
  }

  /**
   * Gets the value of the 'timeStarted' field.
   */
  public java.lang.Long getTimeStarted() {
    return timeStarted;
  }

  /**
   * Sets the value of the 'timeStarted' field.
   * @param value the value to set.
   */
  public void setTimeStarted(java.lang.Long value) {
    this.timeStarted = value;
  }

  /**
   * Gets the value of the 'SupportedChannelsArray' field.
   */
  public java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> getSupportedChannelsArray() {
    return SupportedChannelsArray;
  }

  /**
   * Sets the value of the 'SupportedChannelsArray' field.
   * @param value the value to set.
   */
  public void setSupportedChannelsArray(java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> value) {
    this.SupportedChannelsArray = value;
  }

  /** Creates a new OperationsNodeInfo RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder();
  }

  /** Creates a new OperationsNodeInfo RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder(other);
  }

  /** Creates a new OperationsNodeInfo RecordBuilder by copying an existing OperationsNodeInfo instance */
  public static org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo other) {
    return new org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder(other);
  }

  /**
   * RecordBuilder for OperationsNodeInfo instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<OperationsNodeInfo>
    implements org.apache.avro.data.RecordBuilder<OperationsNodeInfo> {

    private org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo;
    private long timeStarted;
    private java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> SupportedChannelsArray;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.connectionInfo)) {
        this.connectionInfo = data().deepCopy(fields()[0].schema(), other.connectionInfo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeStarted)) {
        this.timeStarted = data().deepCopy(fields()[1].schema(), other.timeStarted);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.SupportedChannelsArray)) {
        this.SupportedChannelsArray = data().deepCopy(fields()[2].schema(), other.SupportedChannelsArray);
        fieldSetFlags()[2] = true;
      }
    }

    /** Creates a Builder by copying an existing OperationsNodeInfo instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo other) {
            super(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.SCHEMA$);
      if (isValidValue(fields()[0], other.connectionInfo)) {
        this.connectionInfo = data().deepCopy(fields()[0].schema(), other.connectionInfo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timeStarted)) {
        this.timeStarted = data().deepCopy(fields()[1].schema(), other.timeStarted);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.SupportedChannelsArray)) {
        this.SupportedChannelsArray = data().deepCopy(fields()[2].schema(), other.SupportedChannelsArray);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'connectionInfo' field */
    public org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo getConnectionInfo() {
      return connectionInfo;
    }

    /** Sets the value of the 'connectionInfo' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder setConnectionInfo(org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo value) {
      validate(fields()[0], value);
      this.connectionInfo = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'connectionInfo' field has been set */
    public boolean hasConnectionInfo() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'connectionInfo' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder clearConnectionInfo() {
      connectionInfo = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'timeStarted' field */
    public java.lang.Long getTimeStarted() {
      return timeStarted;
    }

    /** Sets the value of the 'timeStarted' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder setTimeStarted(long value) {
      validate(fields()[1], value);
      this.timeStarted = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /** Checks whether the 'timeStarted' field has been set */
    public boolean hasTimeStarted() {
      return fieldSetFlags()[1];
    }

    /** Clears the value of the 'timeStarted' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder clearTimeStarted() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'SupportedChannelsArray' field */
    public java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> getSupportedChannelsArray() {
      return SupportedChannelsArray;
    }

    /** Sets the value of the 'SupportedChannelsArray' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder setSupportedChannelsArray(java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel> value) {
      validate(fields()[2], value);
      this.SupportedChannelsArray = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /** Checks whether the 'SupportedChannelsArray' field has been set */
    public boolean hasSupportedChannelsArray() {
      return fieldSetFlags()[2];
    }

    /** Clears the value of the 'SupportedChannelsArray' field */
    public org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo.Builder clearSupportedChannelsArray() {
      SupportedChannelsArray = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public OperationsNodeInfo build() {
      try {
        OperationsNodeInfo record = new OperationsNodeInfo();
        record.connectionInfo = fieldSetFlags()[0] ? this.connectionInfo : (org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo) defaultValue(fields()[0]);
        record.timeStarted = fieldSetFlags()[1] ? this.timeStarted : (java.lang.Long) defaultValue(fields()[1]);
        record.SupportedChannelsArray = fieldSetFlags()[2] ? this.SupportedChannelsArray : (java.util.List<org.kaaproject.kaa.server.common.zk.gen.SupportedChannel>) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
