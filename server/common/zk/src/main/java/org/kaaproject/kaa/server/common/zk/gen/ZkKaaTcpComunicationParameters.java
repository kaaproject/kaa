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
public class ZkKaaTcpComunicationParameters extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ZkKaaTcpComunicationParameters\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters ZkComunicationParameters;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public ZkKaaTcpComunicationParameters() {}

  /**
   * All-args constructor.
   */
  public ZkKaaTcpComunicationParameters(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters ZkComunicationParameters) {
    this.ZkComunicationParameters = ZkComunicationParameters;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return ZkComunicationParameters;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: ZkComunicationParameters = (org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'ZkComunicationParameters' field.
   */
  public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters getZkComunicationParameters() {
    return ZkComunicationParameters;
  }

  /**
   * Sets the value of the 'ZkComunicationParameters' field.
   * @param value the value to set.
   */
  public void setZkComunicationParameters(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters value) {
    this.ZkComunicationParameters = value;
  }

  /** Creates a new ZkKaaTcpComunicationParameters RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder();
  }

  /** Creates a new ZkKaaTcpComunicationParameters RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder(other);
  }

  /** Creates a new ZkKaaTcpComunicationParameters RecordBuilder by copying an existing ZkKaaTcpComunicationParameters instance */
  public static org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters other) {
    return new org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder(other);
  }

  /**
   * RecordBuilder for ZkKaaTcpComunicationParameters instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ZkKaaTcpComunicationParameters>
    implements org.apache.avro.data.RecordBuilder<ZkKaaTcpComunicationParameters> {

    private org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters ZkComunicationParameters;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.ZkComunicationParameters)) {
        this.ZkComunicationParameters = data().deepCopy(fields()[0].schema(), other.ZkComunicationParameters);
        fieldSetFlags()[0] = true;
      }
    }

    /** Creates a Builder by copying an existing ZkKaaTcpComunicationParameters instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters other) {
            super(org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.SCHEMA$);
      if (isValidValue(fields()[0], other.ZkComunicationParameters)) {
        this.ZkComunicationParameters = data().deepCopy(fields()[0].schema(), other.ZkComunicationParameters);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'ZkComunicationParameters' field */
    public org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters getZkComunicationParameters() {
      return ZkComunicationParameters;
    }

    /** Sets the value of the 'ZkComunicationParameters' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder setZkComunicationParameters(org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters value) {
      validate(fields()[0], value);
      this.ZkComunicationParameters = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'ZkComunicationParameters' field has been set */
    public boolean hasZkComunicationParameters() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'ZkComunicationParameters' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters.Builder clearZkComunicationParameters() {
      ZkComunicationParameters = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public ZkKaaTcpComunicationParameters build() {
      try {
        ZkKaaTcpComunicationParameters record = new ZkKaaTcpComunicationParameters();
        record.ZkComunicationParameters = fieldSetFlags()[0] ? this.ZkComunicationParameters : (org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
