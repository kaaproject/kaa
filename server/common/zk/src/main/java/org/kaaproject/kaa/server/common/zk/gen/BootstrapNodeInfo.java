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
public class BootstrapNodeInfo extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"BootstrapNodeInfo\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"connectionInfo\",\"type\":{\"type\":\"record\",\"name\":\"ConnectionInfo\",\"fields\":[{\"name\":\"thriftHost\",\"type\":\"string\"},{\"name\":\"thriftPort\",\"type\":\"int\"},{\"name\":\"publicKey\",\"type\":[\"bytes\",\"null\"]}]}},{\"name\":\"BootstrapHostName\",\"type\":\"string\"},{\"name\":\"BootstrapPort\",\"type\":\"int\"},{\"name\":\"processedRequestCount\",\"type\":\"int\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo;
   private java.lang.CharSequence BootstrapHostName;
   private int BootstrapPort;
   private int processedRequestCount;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public BootstrapNodeInfo() {}

  /**
   * All-args constructor.
   */
  public BootstrapNodeInfo(org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo, java.lang.CharSequence BootstrapHostName, java.lang.Integer BootstrapPort, java.lang.Integer processedRequestCount) {
    this.connectionInfo = connectionInfo;
    this.BootstrapHostName = BootstrapHostName;
    this.BootstrapPort = BootstrapPort;
    this.processedRequestCount = processedRequestCount;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return connectionInfo;
    case 1: return BootstrapHostName;
    case 2: return BootstrapPort;
    case 3: return processedRequestCount;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: connectionInfo = (org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo)value$; break;
    case 1: BootstrapHostName = (java.lang.CharSequence)value$; break;
    case 2: BootstrapPort = (java.lang.Integer)value$; break;
    case 3: processedRequestCount = (java.lang.Integer)value$; break;
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
   * Gets the value of the 'BootstrapHostName' field.
   */
  public java.lang.CharSequence getBootstrapHostName() {
    return BootstrapHostName;
  }

  /**
   * Sets the value of the 'BootstrapHostName' field.
   * @param value the value to set.
   */
  public void setBootstrapHostName(java.lang.CharSequence value) {
    this.BootstrapHostName = value;
  }

  /**
   * Gets the value of the 'BootstrapPort' field.
   */
  public java.lang.Integer getBootstrapPort() {
    return BootstrapPort;
  }

  /**
   * Sets the value of the 'BootstrapPort' field.
   * @param value the value to set.
   */
  public void setBootstrapPort(java.lang.Integer value) {
    this.BootstrapPort = value;
  }

  /**
   * Gets the value of the 'processedRequestCount' field.
   */
  public java.lang.Integer getProcessedRequestCount() {
    return processedRequestCount;
  }

  /**
   * Sets the value of the 'processedRequestCount' field.
   * @param value the value to set.
   */
  public void setProcessedRequestCount(java.lang.Integer value) {
    this.processedRequestCount = value;
  }

  /** Creates a new BootstrapNodeInfo RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder();
  }

  /** Creates a new BootstrapNodeInfo RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder(other);
  }

  /** Creates a new BootstrapNodeInfo RecordBuilder by copying an existing BootstrapNodeInfo instance */
  public static org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo other) {
    return new org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder(other);
  }

  /**
   * RecordBuilder for BootstrapNodeInfo instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<BootstrapNodeInfo>
    implements org.apache.avro.data.RecordBuilder<BootstrapNodeInfo> {

    private org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo connectionInfo;
    private java.lang.CharSequence BootstrapHostName;
    private int BootstrapPort;
    private int processedRequestCount;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.connectionInfo)) {
        this.connectionInfo = data().deepCopy(fields()[0].schema(), other.connectionInfo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.BootstrapHostName)) {
        this.BootstrapHostName = data().deepCopy(fields()[1].schema(), other.BootstrapHostName);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.BootstrapPort)) {
        this.BootstrapPort = data().deepCopy(fields()[2].schema(), other.BootstrapPort);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.processedRequestCount)) {
        this.processedRequestCount = data().deepCopy(fields()[3].schema(), other.processedRequestCount);
        fieldSetFlags()[3] = true;
      }
    }

    /** Creates a Builder by copying an existing BootstrapNodeInfo instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo other) {
            super(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.SCHEMA$);
      if (isValidValue(fields()[0], other.connectionInfo)) {
        this.connectionInfo = data().deepCopy(fields()[0].schema(), other.connectionInfo);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.BootstrapHostName)) {
        this.BootstrapHostName = data().deepCopy(fields()[1].schema(), other.BootstrapHostName);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.BootstrapPort)) {
        this.BootstrapPort = data().deepCopy(fields()[2].schema(), other.BootstrapPort);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.processedRequestCount)) {
        this.processedRequestCount = data().deepCopy(fields()[3].schema(), other.processedRequestCount);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'connectionInfo' field */
    public org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo getConnectionInfo() {
      return connectionInfo;
    }

    /** Sets the value of the 'connectionInfo' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder setConnectionInfo(org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo value) {
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
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder clearConnectionInfo() {
      connectionInfo = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'BootstrapHostName' field */
    public java.lang.CharSequence getBootstrapHostName() {
      return BootstrapHostName;
    }

    /** Sets the value of the 'BootstrapHostName' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder setBootstrapHostName(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.BootstrapHostName = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /** Checks whether the 'BootstrapHostName' field has been set */
    public boolean hasBootstrapHostName() {
      return fieldSetFlags()[1];
    }

    /** Clears the value of the 'BootstrapHostName' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder clearBootstrapHostName() {
      BootstrapHostName = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'BootstrapPort' field */
    public java.lang.Integer getBootstrapPort() {
      return BootstrapPort;
    }

    /** Sets the value of the 'BootstrapPort' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder setBootstrapPort(int value) {
      validate(fields()[2], value);
      this.BootstrapPort = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /** Checks whether the 'BootstrapPort' field has been set */
    public boolean hasBootstrapPort() {
      return fieldSetFlags()[2];
    }

    /** Clears the value of the 'BootstrapPort' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder clearBootstrapPort() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'processedRequestCount' field */
    public java.lang.Integer getProcessedRequestCount() {
      return processedRequestCount;
    }

    /** Sets the value of the 'processedRequestCount' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder setProcessedRequestCount(int value) {
      validate(fields()[3], value);
      this.processedRequestCount = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /** Checks whether the 'processedRequestCount' field has been set */
    public boolean hasProcessedRequestCount() {
      return fieldSetFlags()[3];
    }

    /** Clears the value of the 'processedRequestCount' field */
    public org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo.Builder clearProcessedRequestCount() {
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public BootstrapNodeInfo build() {
      try {
        BootstrapNodeInfo record = new BootstrapNodeInfo();
        record.connectionInfo = fieldSetFlags()[0] ? this.connectionInfo : (org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo) defaultValue(fields()[0]);
        record.BootstrapHostName = fieldSetFlags()[1] ? this.BootstrapHostName : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.BootstrapPort = fieldSetFlags()[2] ? this.BootstrapPort : (java.lang.Integer) defaultValue(fields()[2]);
        record.processedRequestCount = fieldSetFlags()[3] ? this.processedRequestCount : (java.lang.Integer) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
