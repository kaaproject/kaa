package org.kaaproject.kaa.client.plugin.messaging.ext1.avro;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ClassB extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ClassB\",\"namespace\":\"org.kaaproject.kaa.server.plugin.messaging.gen.test\",\"fields\":[]}");
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

  /** Creates a new ClassB RecordBuilder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder newBuilder() {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder();
  }
  
  /** Creates a new ClassB RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder(other);
  }
  
  /** Creates a new ClassB RecordBuilder by copying an existing ClassB instance */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder(other);
  }
  
  /**
   * RecordBuilder for ClassB instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ClassB>
    implements org.apache.avro.data.RecordBuilder<ClassB> {


    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing ClassB instance */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB other) {
            super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB.SCHEMA$);
    }

    @Override
    public ClassB build() {
      try {
        ClassB record = new ClassB();
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
