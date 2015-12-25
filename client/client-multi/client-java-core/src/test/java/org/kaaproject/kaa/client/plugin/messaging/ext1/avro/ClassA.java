package org.kaaproject.kaa.client.plugin.messaging.ext1.avro;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ClassA extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ClassA\",\"namespace\":\"org.kaaproject.kaa.server.plugin.messaging.gen.test\",\"fields\":[]}");
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

  /** Creates a new ClassA RecordBuilder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder newBuilder() {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder();
  }
  
  /** Creates a new ClassA RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder(other);
  }
  
  /** Creates a new ClassA RecordBuilder by copying an existing ClassA instance */
  public static org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder newBuilder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA other) {
    return new org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder(other);
  }
  
  /**
   * RecordBuilder for ClassA instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ClassA>
    implements org.apache.avro.data.RecordBuilder<ClassA> {


    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing ClassA instance */
    private Builder(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA other) {
            super(org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA.SCHEMA$);
    }

    @Override
    public ClassA build() {
      try {
        ClassA record = new ClassA();
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
