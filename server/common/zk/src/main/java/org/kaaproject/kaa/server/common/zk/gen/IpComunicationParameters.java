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
public class IpComunicationParameters extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private java.lang.CharSequence HostName;
   private int Port;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public IpComunicationParameters() {}

  /**
   * All-args constructor.
   */
  public IpComunicationParameters(java.lang.CharSequence HostName, java.lang.Integer Port) {
    this.HostName = HostName;
    this.Port = Port;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return HostName;
    case 1: return Port;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: HostName = (java.lang.CharSequence)value$; break;
    case 1: Port = (java.lang.Integer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'HostName' field.
   */
  public java.lang.CharSequence getHostName() {
    return HostName;
  }

  /**
   * Sets the value of the 'HostName' field.
   * @param value the value to set.
   */
  public void setHostName(java.lang.CharSequence value) {
    this.HostName = value;
  }

  /**
   * Gets the value of the 'Port' field.
   */
  public java.lang.Integer getPort() {
    return Port;
  }

  /**
   * Sets the value of the 'Port' field.
   * @param value the value to set.
   */
  public void setPort(java.lang.Integer value) {
    this.Port = value;
  }

  /** Creates a new IpComunicationParameters RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder();
  }

  /** Creates a new IpComunicationParameters RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder(other);
  }

  /** Creates a new IpComunicationParameters RecordBuilder by copying an existing IpComunicationParameters instance */
  public static org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters other) {
    return new org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder(other);
  }

  /**
   * RecordBuilder for IpComunicationParameters instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<IpComunicationParameters>
    implements org.apache.avro.data.RecordBuilder<IpComunicationParameters> {

    private java.lang.CharSequence HostName;
    private int Port;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.HostName)) {
        this.HostName = data().deepCopy(fields()[0].schema(), other.HostName);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.Port)) {
        this.Port = data().deepCopy(fields()[1].schema(), other.Port);
        fieldSetFlags()[1] = true;
      }
    }

    /** Creates a Builder by copying an existing IpComunicationParameters instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters other) {
            super(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.SCHEMA$);
      if (isValidValue(fields()[0], other.HostName)) {
        this.HostName = data().deepCopy(fields()[0].schema(), other.HostName);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.Port)) {
        this.Port = data().deepCopy(fields()[1].schema(), other.Port);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'HostName' field */
    public java.lang.CharSequence getHostName() {
      return HostName;
    }

    /** Sets the value of the 'HostName' field */
    public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder setHostName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.HostName = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'HostName' field has been set */
    public boolean hasHostName() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'HostName' field */
    public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder clearHostName() {
      HostName = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'Port' field */
    public java.lang.Integer getPort() {
      return Port;
    }

    /** Sets the value of the 'Port' field */
    public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder setPort(int value) {
      validate(fields()[1], value);
      this.Port = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /** Checks whether the 'Port' field has been set */
    public boolean hasPort() {
      return fieldSetFlags()[1];
    }

    /** Clears the value of the 'Port' field */
    public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters.Builder clearPort() {
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public IpComunicationParameters build() {
      try {
        IpComunicationParameters record = new IpComunicationParameters();
        record.HostName = fieldSetFlags()[0] ? this.HostName : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.Port = fieldSetFlags()[1] ? this.Port : (java.lang.Integer) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
