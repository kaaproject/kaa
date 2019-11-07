package org.kaaproject.tutorials;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

import java.io.ByteArrayInputStream;

public class AvroConverter<T> {
  private final ThreadLocal<BinaryDecoder> threadLocalDecoder = new ThreadLocal<>();
  private final DatumReader<T> datumReader;

  /**
   * Creates converter instance for specified class type.
   * @param type - object type for which this converter will be initialized
   */
  public AvroConverter(Class<T> type) {
    this.datumReader = new SpecificDatumReader<>(type);
  }

  /**
   * Converts byte array into Java class passed in constructor.
   * @return Java class
   */
  public T decode(byte[] bytes) {
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    Decoder decoder = getDecoder(byteIn);
    try {
      return datumReader.read(null, decoder);
    } catch (Exception ex) {
      String msg = "Failed to deserialize submitted byte array into object.";
      throw new RuntimeException(msg, ex);
    }
  }

  private BinaryDecoder getDecoder(ByteArrayInputStream byteIn) {
    BinaryDecoder reuse = threadLocalDecoder.get();
    BinaryDecoder decoder = DecoderFactory.get().directBinaryDecoder(byteIn, reuse);
    if (reuse == null) {
      threadLocalDecoder.set(decoder);
    }
    return decoder;
  }
}
