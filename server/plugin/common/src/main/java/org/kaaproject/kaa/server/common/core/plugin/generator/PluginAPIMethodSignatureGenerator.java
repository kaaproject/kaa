package org.kaaproject.kaa.server.common.core.plugin.generator;

/**
 * An object that generates method signatures for plugin API source file.
 * 
 * @author Bohdan Khablenko
 */
public interface PluginAPIMethodSignatureGenerator {

    public static final String METHOD_SIGNATURE_TEMPLATE = "{0} {1}({2})";

    String generateMethodSignature(String methodName, String paramType, String returnType);
}
