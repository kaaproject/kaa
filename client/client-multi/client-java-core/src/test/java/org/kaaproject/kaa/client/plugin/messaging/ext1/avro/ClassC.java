package org.kaaproject.kaa.client.plugin.messaging.ext1.avro;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ClassC extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ClassC\",\"namespace\":\"org.kaaproject.kaa.server.plugin.messaging.gen.test\",\"fields\":[]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /** Creates a new ClassC RecordBuilder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder newBuilder() {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder();
  }
  
  /** Creates a new ClassC RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder(other);
  }
  
  /** Creates a new ClassC RecordBuilder by copying an existing ClassC instance */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder(other);
  }
  
  /**
   * RecordBuilder for ClassC instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ClassC>
    implements org.apache.avro.data.RecordBuilder<ClassC> {


    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing ClassC instance */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC other) {
            super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC.SCHEMA$);
    }

    @Override
    public ClassC build() {
      try {
        ClassC record = new ClassC();
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
