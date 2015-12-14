package org.kaaproject.kaa.server.common.core.plugin.generator;

/**
 * An object that generates method signatures for plugin API source file.
 * 
 * @author Bohdan Khablenko
 */
public interface MethodSignatureGenerator {

    MethodSignature generateMethodSignature(String methodName, String paramType, String returnType);
}
