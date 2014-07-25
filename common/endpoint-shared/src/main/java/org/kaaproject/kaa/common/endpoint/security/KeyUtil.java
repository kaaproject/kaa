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

package org.kaaproject.kaa.common.endpoint.security;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class KeyUtil is used to persist and fetch Public and Private Keys.
 *
 * @author Andrew Shvayka
 */
public abstract class KeyUtil {
    private static final Logger LOG = LoggerFactory.getLogger(KeyUtil.class);
    private static final String RSA = "RSA";

    /**
     * Saves public and private keys to specified files.
     *
     * @param keyPair the key pair
     * @param privateKeyFile the private key file
     * @param publicKeyFile the public key file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void saveKeyPair(KeyPair keyPair, String privateKeyFile, String publicKeyFile) throws IOException {
        File privateFile = makeDirs(privateKeyFile);
        File publicFile = makeDirs(publicKeyFile);
        OutputStream privateKeyOutput = new FileOutputStream(privateFile);
        OutputStream publicKeyOutput = new FileOutputStream(publicFile);
        saveKeyPair(keyPair, privateKeyOutput, publicKeyOutput);
    }
    
    /**
     * Saves public and private keys to specified streams.
     *
     * @param keyPair the key pair
     * @param privateKeyOutput the private key output stream
     * @param publicKeyOutput the public key output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void saveKeyPair(KeyPair keyPair, OutputStream privateKeyOutput, OutputStream publicKeyOutput) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        try{
            publicKeyOutput.write(x509EncodedKeySpec.getEncoded());
        }finally{
            publicKeyOutput.close();
        }

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        try{
            privateKeyOutput.write(pkcs8EncodedKeySpec.getEncoded());
        }finally{
            privateKeyOutput.close();
        }
    }

    /**
     * Create all required directories
     *
     * @param privateKeyFile the private key file
     * @return the file
     */
    private static File makeDirs(String privateKeyFile) {
        File privateFile = new File(privateKeyFile);
        if(privateFile.getParentFile() != null && !privateFile.getParentFile().exists() && !privateFile.getParentFile().mkdirs()){
            LOG.warn("Failed to create required directories: {}", privateFile.getParentFile().getAbsolutePath());
        }
        return privateFile;
    }

    /**
     * Generate key pair and saves it to specified files.
     *
     * @param privateKeyLocation the private key location
     * @param publicKeyLocation the public key location
     * @return the key pair
     */
    public static KeyPair generateKeyPair(String privateKeyLocation, String publicKeyLocation) {
        try {
            KeyPair clientKeyPair = generateKeyPair();
            saveKeyPair(clientKeyPair, privateKeyLocation, publicKeyLocation);
            return clientKeyPair;
        } catch (Exception e) {
            LOG.error("Error generating client key pair", e);
        }
        return null;
    }
    
    /**
     * Generate key pair and saves it to specified streams.
     *
     * @param privateKeyOutput the private key output stream
     * @param publicKeyOutput the public key output stream
     * @return the key pair
     */
    public static KeyPair generateKeyPair(OutputStream privateKeyOutput, OutputStream publicKeyOutput) {
        try {
            KeyPair clientKeyPair = generateKeyPair();
            saveKeyPair(clientKeyPair, privateKeyOutput, publicKeyOutput);
            return clientKeyPair;
        } catch (Exception e) {
            LOG.error("Error generating client key pair", e);
        }
        return null;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance(RSA);
        clientKeyGen.initialize(2048);
        return clientKeyGen.genKeyPair();
    }

    /**
     * Gets the public key from file.
     *
     * @param f the f
     * @return the public
     * @throws Exception the exception
     */
    public static PublicKey getPublic(File f) throws Exception { //NOSONAR
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        return getPublic(keyBytes);
    }
    
    /**
     * Gets the public key from input stream.
     *
     * @param input the input stream
     * @return the public
     * @throws Exception the exception
     */
    public static PublicKey getPublic(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        input.close();
        byte[] keyBytes = output.toByteArray();
        
        return getPublic(keyBytes);
    }

    /**
     * Gets the public key from bytes.
     *
     * @param keyBytes the key bytes
     * @return the public
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeySpecException the invalid key spec exception
     */
    public static PublicKey getPublic(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePublic(spec);
    }

    /**
     * Gets the private key from file.
     *
     * @param f the f
     * @return the private
     * @throws Exception the exception
     */
    public static PrivateKey getPrivate(File f) throws Exception { //NOSONAR
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        return getPrivate(keyBytes);
    }
    
    /**
     * Gets the private key from input stream.
     *
     * @param input the input stream
     * @return the private
     * @throws Exception the exception
     */
    public static PrivateKey getPrivate(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        input.close();
        byte[] keyBytes = output.toByteArray();
        
        return getPrivate(keyBytes);
    }

    /**
     * Gets the private key from bytes.
     *
     * @param keyBytes the key bytes
     * @return the private
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeySpecException the invalid key spec exception
     */
    public static PrivateKey getPrivate(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePrivate(spec);
    }

}
