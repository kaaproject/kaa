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

package org.kaaproject.kaa.client.logging;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class SuperRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SuperRecord\",\"namespace\":\"org.kaaproject.kaa.client.logging\",\"fields\":[{\"name\":\"logdata\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.String logdata;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public SuperRecord() {}

  /**
   * All-args constructor.
   */
  public SuperRecord(java.lang.String logdata) {
    this.logdata = logdata;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return logdata;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: logdata = (java.lang.String)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'logdata' field.
   */
  public java.lang.String getLogdata() {
    return logdata;
  }

  /**
   * Sets the value of the 'logdata' field.
   * @param value the value to set.
   */
  public void setLogdata(java.lang.String value) {
    this.logdata = value;
  }

  /** Creates a new SuperRecord RecordBuilder */
  public static org.kaaproject.kaa.client.logging.SuperRecord.Builder newBuilder() {
    return new org.kaaproject.kaa.client.logging.SuperRecord.Builder();
  }
  
  /** Creates a new SuperRecord RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.client.logging.SuperRecord.Builder newBuilder(org.kaaproject.kaa.client.logging.SuperRecord.Builder other) {
    return new org.kaaproject.kaa.client.logging.SuperRecord.Builder(other);
  }
  
  /** Creates a new SuperRecord RecordBuilder by copying an existing SuperRecord instance */
  public static org.kaaproject.kaa.client.logging.SuperRecord.Builder newBuilder(org.kaaproject.kaa.client.logging.SuperRecord other) {
    return new org.kaaproject.kaa.client.logging.SuperRecord.Builder(other);
  }
  
  /**
   * RecordBuilder for SuperRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<SuperRecord>
    implements org.apache.avro.data.RecordBuilder<SuperRecord> {

    private java.lang.String logdata;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.client.logging.SuperRecord.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.client.logging.SuperRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.logdata)) {
        this.logdata = data().deepCopy(fields()[0].schema(), other.logdata);
        fieldSetFlags()[0] = true;
      }
    }
    
    /** Creates a Builder by copying an existing SuperRecord instance */
    private Builder(org.kaaproject.kaa.client.logging.SuperRecord other) {
            super(org.kaaproject.kaa.client.logging.SuperRecord.SCHEMA$);
      if (isValidValue(fields()[0], other.logdata)) {
        this.logdata = data().deepCopy(fields()[0].schema(), other.logdata);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'logdata' field */
    public java.lang.String getLogdata() {
      return logdata;
    }
    
    /** Sets the value of the 'logdata' field */
    public org.kaaproject.kaa.client.logging.SuperRecord.Builder setLogdata(java.lang.String value) {
      validate(fields()[0], value);
      this.logdata = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'logdata' field has been set */
    public boolean hasLogdata() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'logdata' field */
    public org.kaaproject.kaa.client.logging.SuperRecord.Builder clearLogdata() {
      logdata = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public SuperRecord build() {
      try {
        SuperRecord record = new SuperRecord();
        record.logdata = fieldSetFlags()[0] ? this.logdata : (java.lang.String) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
