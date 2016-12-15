package org.kaaproject.kaa.server.common.utils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;

public final class Utils {

  private Utils() {}

  /**
   * Encode byte array into string if it is not null.
   *
   * @param buf byte array to be encoded
   * @return    encoded string
   */
  public static String encodeHexString(byte[] buf) {
    return buf != null ? Hex.encodeHexString(buf) : "";
  }

  public static String encodeHexString(ByteBuffer buf) {
    return buf != null ? Hex.encodeHexString(buf.array()) : "";
  }
}
