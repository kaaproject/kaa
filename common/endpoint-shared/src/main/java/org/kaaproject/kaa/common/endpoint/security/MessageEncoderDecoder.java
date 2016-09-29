/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.common.endpoint.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * The Class MessageEncoderDecoder is responsible for encoding/decoding logic of
 * endpoint - operations service communication.
 *
 * @author Andrew Shvayka
 */
public class MessageEncoderDecoder {

  protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray(); // NOSONAR
  private static final Logger LOG = LoggerFactory.getLogger(MessageEncoderDecoder.class);
  private static final String SESSION_CRYPT_ALGORITHM = "AES/ECB/PKCS5PADDING";
  private static final String SESSION_KEY_ALGORITHM = "AES";
  private static final int SESSION_KEY_SIZE = 128;
  private static final String SHA1WITH_RSA = "SHA1withRSA";
  private static final String RSA = "RSA/ECB/PKCS1Padding";
  private static final ThreadLocal<Cipher> RSA_CIPHER = new ThreadLocal<Cipher>() {
    @Override
    protected Cipher initialValue() {
      return cipherForAlgorithm(RSA);
    }
  };
  private static final ThreadLocal<Signature> SHA1WITH_RSA_SIGNATURE = new
          ThreadLocal<Signature>() {
    @Override
    protected Signature initialValue() {
      return signatureForAlgorithm(SHA1WITH_RSA);
    }
  };

  private static final ThreadLocal<KeyGenerator> SESSION_KEY_GENERATOR = new
          ThreadLocal<KeyGenerator>() {
    @Override
    protected KeyGenerator initialValue() {
      return keyGeneratorForAlgorithm(SESSION_KEY_ALGORITHM, SESSION_KEY_SIZE);
    }
  };
  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private PublicKey remotePublicKey;
  private SecretKey sessionKey;
  private CipherPair sessionCipherPair;

  /**
   * Instantiates a new message encoder decoder.
   *
   * @param privateKey the private key
   * @param publicKey  the public key
   */
  public MessageEncoderDecoder(PrivateKey privateKey, PublicKey publicKey) {
    this(privateKey, publicKey, null);
  }

  /**
   * Instantiates a new message encoder decoder.
   *
   * @param privateKey      the private key
   * @param publicKey       the public key
   * @param remotePublicKey the remote public key
   */
  public MessageEncoderDecoder(
          PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey) {
    super();
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.remotePublicKey = remotePublicKey;

    if (LOG.isTraceEnabled()) {
      LOG.trace("Creating MessageEncoderDecoder with\nPublicKey {};\nRemotePublicKey {}",
          this.publicKey != null ? bytesToHex(this.publicKey.getEncoded()) : "empty",
          this.remotePublicKey != null ? bytesToHex(this.remotePublicKey.getEncoded()) : "empty");
    }
  }

  static Cipher cipherForAlgorithm(String algorithm) {
    try {
      return Cipher.getInstance(algorithm);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
      LOG.error("Cipher init error", ex);
      return null;
    }
  }

  static KeyGenerator keyGeneratorForAlgorithm(String algorithm, int size) {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
      keyGen.init(size);
      return keyGen;
    } catch (NoSuchAlgorithmException ex) {
      LOG.error("Key generator init error", ex);
      return null;
    }
  }

  static Signature signatureForAlgorithm(String algorithm) {
    try {
      return Signature.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      LOG.error("Signature init error", ex);
      return null;
    }
  }

  /**
   *  Convert bytes to hex format.
   * @param   bytes the input bytes
   * @return  the string of converted bytes in hex format
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 3];
    for (int j = 0; j < bytes.length; j++) {
      int value = bytes[j] & 0xFF;
      hexChars[j * 3] = HEX_ARRAY[value >>> 4];
      hexChars[j * 3 + 1] = HEX_ARRAY[value & 0x0F];
      hexChars[j * 3 + 2] = ' ';
    }
    return new String(hexChars);
  }

  /**
   * Gets the encoded session key.
   *
   * @return the encoded session key
   * @throws GeneralSecurityException the general security exception
   */
  public byte[] getEncodedSessionKey() throws GeneralSecurityException {
    SecretKey key = getSessionKey();
    Cipher keyCipher = RSA_CIPHER.get();
    keyCipher.init(Cipher.ENCRYPT_MODE, remotePublicKey);
    return keyCipher.doFinal(key.getEncoded());
  }

  /**
   * Encode data using sessionKey.
   *
   * @param message the message
   * @return the byte[]
   * @throws GeneralSecurityException the general security exception
   */
  public byte[] encodeData(byte[] message) throws GeneralSecurityException {
    if (sessionCipherPair == null) {
      sessionCipherPair = new CipherPair(SESSION_CRYPT_ALGORITHM, getSessionKey());
    }
    return sessionCipherPair.encCipher.doFinal(message);
  }


  private void decodeSessionKey(byte[] encodedKey) throws
      InvalidKeyException,
      IllegalBlockSizeException,
      BadPaddingException {
    Cipher sessionKeyCipher = RSA_CIPHER.get();
    sessionKeyCipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] sessionKeyBytes = sessionKeyCipher.doFinal(encodedKey);
    sessionKey = new SecretKeySpec(sessionKeyBytes, 0, SESSION_KEY_SIZE / 8, SESSION_KEY_ALGORITHM);
  }

  /**
   * Decode data using session key.
   *
   * @param message the message
   * @return the byte[]
   * @throws GeneralSecurityException the general security exception
   */
  public byte[] decodeData(byte[] message) throws GeneralSecurityException {
    if (sessionCipherPair == null) {
      sessionCipherPair = new CipherPair(SESSION_CRYPT_ALGORITHM, getSessionKey());
    }
    return sessionCipherPair.decCipher.doFinal(message);
  }


  /**
   * Decode data using session key then is decoded using private key.
   *
   * @param message    the message
   * @param encodedKey the encoded key
   * @return the byte[]
   * @throws GeneralSecurityException the general security exception
   */
  public byte[] decodeData(byte[] message, byte[] encodedKey) throws GeneralSecurityException {
    sessionCipherPair = null;
    decodeSessionKey(encodedKey);
    return decodeData(message);
  }

  /**
   * Sign message using private key.
   *
   * @param message the message
   * @return the byte[]
   * @throws GeneralSecurityException the general security exception
   */
  public byte[] sign(byte[] message) throws GeneralSecurityException {
    Signature signer = SHA1WITH_RSA_SIGNATURE.get();
    signer.initSign(privateKey);
    signer.update(message);
    return signer.sign();
  }

  /**
   * Verify message using signature and remote public key.
   *
   * @param message   the message
   * @param signature the signature
   * @return true, if successful
   * @throws GeneralSecurityException the general security exception
   */
  public boolean verify(byte[] message, byte[] signature) throws GeneralSecurityException {
    Signature verifier = SHA1WITH_RSA_SIGNATURE.get();
    verifier.initVerify(remotePublicKey);
    verifier.update(message);
    return verifier.verify(signature);
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * Gets the public key.
   *
   * @return the public key
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * Gets the remote public key.
   *
   * @return the remote public key
   */
  public PublicKey getRemotePublicKey() {
    return remotePublicKey;
  }

  /**
   * Sets the remote public key.
   *
   * @param remotePublicKey the new remote public key
   * @throws GeneralSecurityException the general security exception
   */
  public void setRemotePublicKey(byte[] remotePublicKey) throws GeneralSecurityException {
    this.remotePublicKey = KeyUtil.getPublic(remotePublicKey);
    if (LOG.isTraceEnabled()) {
      LOG.trace("RemotePublicKey {}",
          this.remotePublicKey != null ? bytesToHex(this.remotePublicKey.getEncoded()) : "empty");
    }
  }

  /**
   * Sets the remote public key.
   *
   * @param remotePublicKey the new remote public key
   * @throws GeneralSecurityException the general security exception
   */
  public void setRemotePublicKey(PublicKey remotePublicKey) throws GeneralSecurityException {
    this.remotePublicKey = remotePublicKey;
    if (LOG.isTraceEnabled()) {
      LOG.trace("RemotePublicKey {}",
          this.remotePublicKey != null ? bytesToHex(this.remotePublicKey.getEncoded()) : "empty");
    }
  }

  /**
   * Gets the session key.
   *
   * @return the session key
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  private SecretKey getSessionKey() throws NoSuchAlgorithmException {
    if (sessionKey == null) {
      sessionKey = SESSION_KEY_GENERATOR.get().generateKey();
    }
    return sessionKey;
  }

  public CipherPair getSessionCipherPair() {
    return sessionCipherPair;
  }

  public void setSessionCipherPair(CipherPair sessionCipher) {
    this.sessionCipherPair = sessionCipher;
  }

  /**
   * Cipher Pair holds references for encoding and decoding Ciphers that are initialized with the
   * same key.
   */
  public static class CipherPair {
    private Cipher decCipher;
    private Cipher encCipher;

    /**
     * Creates enc/dec ciphers based on cipher algorithm and secret key.
     *
     * @param algorithm - Cipher algorithm
     * @param secretKey - Secret key
     */
    private CipherPair(String algorithm, SecretKey secretKey) throws InvalidKeyException {
      this.decCipher = cipherForAlgorithm(algorithm);
      this.decCipher.init(Cipher.DECRYPT_MODE, secretKey);
      this.encCipher = cipherForAlgorithm(algorithm);
      this.encCipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }
  }

}
