/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.kaaproject.kaa.server.transport.tcp.config.gen;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class AvroTcpConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AvroTcpConfig\",\"namespace\":\"org.kaaproject.kaa.server.transport.tcp.config.gen\",\"fields\":[{\"name\":\"bindInterface\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"bindPort\",\"type\":\"int\"},{\"name\":\"publicInterface\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"publicPorts\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private java.lang.String bindInterface;
   private int bindPort;
   private java.lang.String publicInterface;
   private java.lang.String publicPorts;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public AvroTcpConfig() {}

  /**
   * All-args constructor.
   */
  public AvroTcpConfig(java.lang.String bindInterface, java.lang.Integer bindPort, java.lang.String publicInterface, java.lang.String publicPorts) {
    this.bindInterface = bindInterface;
    this.bindPort = bindPort;
    this.publicInterface = publicInterface;
    this.publicPorts = publicPorts;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return bindInterface;
    case 1: return bindPort;
    case 2: return publicInterface;
    case 3: return publicPorts;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: bindInterface = (java.lang.String)value$; break;
    case 1: bindPort = (java.lang.Integer)value$; break;
    case 2: publicInterface = (java.lang.String)value$; break;
    case 3: publicPorts = (java.lang.String)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'bindInterface' field.
   */
  public java.lang.String getBindInterface() {
    return bindInterface;
  }

  /**
   * Sets the value of the 'bindInterface' field.
   * @param value the value to set.
   */
  public void setBindInterface(java.lang.String value) {
    this.bindInterface = value;
  }

  /**
   * Gets the value of the 'bindPort' field.
   */
  public java.lang.Integer getBindPort() {
    return bindPort;
  }

  /**
   * Sets the value of the 'bindPort' field.
   * @param value the value to set.
   */
  public void setBindPort(java.lang.Integer value) {
    this.bindPort = value;
  }

  /**
   * Gets the value of the 'publicInterface' field.
   */
  public java.lang.String getPublicInterface() {
    return publicInterface;
  }

  /**
   * Sets the value of the 'publicInterface' field.
   * @param value the value to set.
   */
  public void setPublicInterface(java.lang.String value) {
    this.publicInterface = value;
  }

  /**
   * Gets the value of the 'publicPorts' field.
   */
  public java.lang.String getPublicPorts() {
    return publicPorts;
  }

  /**
   * Sets the value of the 'publicPorts' field.
   * @param value the value to set.
   */
  public void setPublicPorts(java.lang.String value) {
    this.publicPorts = value;
  }

  /** Creates a new AvroTcpConfig RecordBuilder */
  public static org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder newBuilder() {
    return new org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder();
  }
  
  /** Creates a new AvroTcpConfig RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder newBuilder(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder other) {
    return new org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder(other);
  }
  
  /** Creates a new AvroTcpConfig RecordBuilder by copying an existing AvroTcpConfig instance */
  public static org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder newBuilder(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig other) {
    return new org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder(other);
  }
  
  /**
   * RecordBuilder for AvroTcpConfig instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AvroTcpConfig>
    implements org.apache.avro.data.RecordBuilder<AvroTcpConfig> {

    private java.lang.String bindInterface;
    private int bindPort;
    private java.lang.String publicInterface;
    private java.lang.String publicPorts;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.bindInterface)) {
        this.bindInterface = data().deepCopy(fields()[0].schema(), other.bindInterface);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.bindPort)) {
        this.bindPort = data().deepCopy(fields()[1].schema(), other.bindPort);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.publicInterface)) {
        this.publicInterface = data().deepCopy(fields()[2].schema(), other.publicInterface);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.publicPorts)) {
        this.publicPorts = data().deepCopy(fields()[3].schema(), other.publicPorts);
        fieldSetFlags()[3] = true;
      }
    }
    
    /** Creates a Builder by copying an existing AvroTcpConfig instance */
    private Builder(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig other) {
            super(org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.SCHEMA$);
      if (isValidValue(fields()[0], other.bindInterface)) {
        this.bindInterface = data().deepCopy(fields()[0].schema(), other.bindInterface);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.bindPort)) {
        this.bindPort = data().deepCopy(fields()[1].schema(), other.bindPort);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.publicInterface)) {
        this.publicInterface = data().deepCopy(fields()[2].schema(), other.publicInterface);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.publicPorts)) {
        this.publicPorts = data().deepCopy(fields()[3].schema(), other.publicPorts);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'bindInterface' field */
    public java.lang.String getBindInterface() {
      return bindInterface;
    }
    
    /** Sets the value of the 'bindInterface' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder setBindInterface(java.lang.String value) {
      validate(fields()[0], value);
      this.bindInterface = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'bindInterface' field has been set */
    public boolean hasBindInterface() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'bindInterface' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder clearBindInterface() {
      bindInterface = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'bindPort' field */
    public java.lang.Integer getBindPort() {
      return bindPort;
    }
    
    /** Sets the value of the 'bindPort' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder setBindPort(int value) {
      validate(fields()[1], value);
      this.bindPort = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'bindPort' field has been set */
    public boolean hasBindPort() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'bindPort' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder clearBindPort() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'publicInterface' field */
    public java.lang.String getPublicInterface() {
      return publicInterface;
    }
    
    /** Sets the value of the 'publicInterface' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder setPublicInterface(java.lang.String value) {
      validate(fields()[2], value);
      this.publicInterface = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'publicInterface' field has been set */
    public boolean hasPublicInterface() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'publicInterface' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder clearPublicInterface() {
      publicInterface = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'publicPorts' field */
    public java.lang.String getPublicPorts() {
      return publicPorts;
    }
    
    /** Sets the value of the 'publicPorts' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder setPublicPorts(java.lang.String value) {
      validate(fields()[3], value);
      this.publicPorts = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'publicPorts' field has been set */
    public boolean hasPublicPorts() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'publicPorts' field */
    public org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig.Builder clearPublicPorts() {
      publicPorts = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public AvroTcpConfig build() {
      try {
        AvroTcpConfig record = new AvroTcpConfig();
        record.bindInterface = fieldSetFlags()[0] ? this.bindInterface : (java.lang.String) defaultValue(fields()[0]);
        record.bindPort = fieldSetFlags()[1] ? this.bindPort : (java.lang.Integer) defaultValue(fields()[1]);
        record.publicInterface = fieldSetFlags()[2] ? this.publicInterface : (java.lang.String) defaultValue(fields()[2]);
        record.publicPorts = fieldSetFlags()[3] ? this.publicPorts : (java.lang.String) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
