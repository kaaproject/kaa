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
public class ZkSupportedChannel extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ZkSupportedChannel\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"ChannelType\",\"type\":{\"type\":\"enum\",\"name\":\"ZkChannelType\",\"symbols\":[\"HTTP\",\"HTTP_LP\"]}},{\"name\":\"RedirectionSupported\",\"type\":\"boolean\"},{\"name\":\"CommunicationParameters\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":\"IpComunicationParameters\"}]}]},{\"name\":\"ChannelStatistics\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":{\"type\":\"record\",\"name\":\"BaseStatistics\",\"fields\":[{\"name\":\"processedRequestCount\",\"type\":\"int\"},{\"name\":\"registeredUsersCount\",\"type\":\"int\"},{\"name\":\"deltaCalculationCount\",\"type\":\"int\"},{\"name\":\"timeStarted\",\"type\":\"long\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":\"BaseStatistics\"}]}]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.ZkChannelType ChannelType;
   private boolean RedirectionSupported;
   private java.lang.Object CommunicationParameters;
   private java.lang.Object ChannelStatistics;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public ZkSupportedChannel() {}

  /**
   * All-args constructor.
   */
  public ZkSupportedChannel(org.kaaproject.kaa.server.common.zk.gen.ZkChannelType ChannelType, java.lang.Boolean RedirectionSupported, java.lang.Object CommunicationParameters, java.lang.Object ChannelStatistics) {
    this.ChannelType = ChannelType;
    this.RedirectionSupported = RedirectionSupported;
    this.CommunicationParameters = CommunicationParameters;
    this.ChannelStatistics = ChannelStatistics;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return ChannelType;
    case 1: return RedirectionSupported;
    case 2: return CommunicationParameters;
    case 3: return ChannelStatistics;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: ChannelType = (org.kaaproject.kaa.server.common.zk.gen.ZkChannelType)value$; break;
    case 1: RedirectionSupported = (java.lang.Boolean)value$; break;
    case 2: CommunicationParameters = value$; break;
    case 3: ChannelStatistics = value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'ChannelType' field.
   */
  public org.kaaproject.kaa.server.common.zk.gen.ZkChannelType getChannelType() {
    return ChannelType;
  }

  /**
   * Sets the value of the 'ChannelType' field.
   * @param value the value to set.
   */
  public void setChannelType(org.kaaproject.kaa.server.common.zk.gen.ZkChannelType value) {
    this.ChannelType = value;
  }

  /**
   * Gets the value of the 'RedirectionSupported' field.
   */
  public java.lang.Boolean getRedirectionSupported() {
    return RedirectionSupported;
  }

  /**
   * Sets the value of the 'RedirectionSupported' field.
   * @param value the value to set.
   */
  public void setRedirectionSupported(java.lang.Boolean value) {
    this.RedirectionSupported = value;
  }

  /**
   * Gets the value of the 'CommunicationParameters' field.
   */
  public java.lang.Object getCommunicationParameters() {
    return CommunicationParameters;
  }

  /**
   * Sets the value of the 'CommunicationParameters' field.
   * @param value the value to set.
   */
  public void setCommunicationParameters(java.lang.Object value) {
    this.CommunicationParameters = value;
  }

  /**
   * Gets the value of the 'ChannelStatistics' field.
   */
  public java.lang.Object getChannelStatistics() {
    return ChannelStatistics;
  }

  /**
   * Sets the value of the 'ChannelStatistics' field.
   * @param value the value to set.
   */
  public void setChannelStatistics(java.lang.Object value) {
    this.ChannelStatistics = value;
  }

  /** Creates a new ZkSupportedChannel RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder();
  }

  /** Creates a new ZkSupportedChannel RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder(other);
  }

  /** Creates a new ZkSupportedChannel RecordBuilder by copying an existing ZkSupportedChannel instance */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel other) {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder(other);
  }

  /**
   * RecordBuilder for ZkSupportedChannel instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ZkSupportedChannel>
    implements org.apache.avro.data.RecordBuilder<ZkSupportedChannel> {

    private org.kaaproject.kaa.server.common.zk.gen.ZkChannelType ChannelType;
    private boolean RedirectionSupported;
    private java.lang.Object CommunicationParameters;
    private java.lang.Object ChannelStatistics;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.ChannelType)) {
        this.ChannelType = data().deepCopy(fields()[0].schema(), other.ChannelType);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.RedirectionSupported)) {
        this.RedirectionSupported = data().deepCopy(fields()[1].schema(), other.RedirectionSupported);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.CommunicationParameters)) {
        this.CommunicationParameters = data().deepCopy(fields()[2].schema(), other.CommunicationParameters);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.ChannelStatistics)) {
        this.ChannelStatistics = data().deepCopy(fields()[3].schema(), other.ChannelStatistics);
        fieldSetFlags()[3] = true;
      }
    }

    /** Creates a Builder by copying an existing ZkSupportedChannel instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel other) {
            super(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.SCHEMA$);
      if (isValidValue(fields()[0], other.ChannelType)) {
        this.ChannelType = data().deepCopy(fields()[0].schema(), other.ChannelType);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.RedirectionSupported)) {
        this.RedirectionSupported = data().deepCopy(fields()[1].schema(), other.RedirectionSupported);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.CommunicationParameters)) {
        this.CommunicationParameters = data().deepCopy(fields()[2].schema(), other.CommunicationParameters);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.ChannelStatistics)) {
        this.ChannelStatistics = data().deepCopy(fields()[3].schema(), other.ChannelStatistics);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'ChannelType' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkChannelType getChannelType() {
      return ChannelType;
    }

    /** Sets the value of the 'ChannelType' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder setChannelType(org.kaaproject.kaa.server.common.zk.gen.ZkChannelType value) {
      validate(fields()[0], value);
      this.ChannelType = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'ChannelType' field has been set */
    public boolean hasChannelType() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'ChannelType' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder clearChannelType() {
      ChannelType = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'RedirectionSupported' field */
    public java.lang.Boolean getRedirectionSupported() {
      return RedirectionSupported;
    }

    /** Sets the value of the 'RedirectionSupported' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder setRedirectionSupported(boolean value) {
      validate(fields()[1], value);
      this.RedirectionSupported = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /** Checks whether the 'RedirectionSupported' field has been set */
    public boolean hasRedirectionSupported() {
      return fieldSetFlags()[1];
    }

    /** Clears the value of the 'RedirectionSupported' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder clearRedirectionSupported() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'CommunicationParameters' field */
    public java.lang.Object getCommunicationParameters() {
      return CommunicationParameters;
    }

    /** Sets the value of the 'CommunicationParameters' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder setCommunicationParameters(java.lang.Object value) {
      validate(fields()[2], value);
      this.CommunicationParameters = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /** Checks whether the 'CommunicationParameters' field has been set */
    public boolean hasCommunicationParameters() {
      return fieldSetFlags()[2];
    }

    /** Clears the value of the 'CommunicationParameters' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder clearCommunicationParameters() {
      CommunicationParameters = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'ChannelStatistics' field */
    public java.lang.Object getChannelStatistics() {
      return ChannelStatistics;
    }

    /** Sets the value of the 'ChannelStatistics' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder setChannelStatistics(java.lang.Object value) {
      validate(fields()[3], value);
      this.ChannelStatistics = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /** Checks whether the 'ChannelStatistics' field has been set */
    public boolean hasChannelStatistics() {
      return fieldSetFlags()[3];
    }

    /** Clears the value of the 'ChannelStatistics' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel.Builder clearChannelStatistics() {
      ChannelStatistics = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public ZkSupportedChannel build() {
      try {
        ZkSupportedChannel record = new ZkSupportedChannel();
        record.ChannelType = fieldSetFlags()[0] ? this.ChannelType : (org.kaaproject.kaa.server.common.zk.gen.ZkChannelType) defaultValue(fields()[0]);
        record.RedirectionSupported = fieldSetFlags()[1] ? this.RedirectionSupported : (java.lang.Boolean) defaultValue(fields()[1]);
        record.CommunicationParameters = fieldSetFlags()[2] ? this.CommunicationParameters : (java.lang.Object) defaultValue(fields()[2]);
        record.ChannelStatistics = fieldSetFlags()[3] ? this.ChannelStatistics : (java.lang.Object) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
