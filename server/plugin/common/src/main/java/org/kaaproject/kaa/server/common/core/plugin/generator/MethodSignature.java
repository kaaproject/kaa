package org.kaaproject.kaa.server.common.core.plugin.generator;

import java.text.MessageFormat;

/**
 * Represents a plugin API method signature.
 * 
 * @author Bohdan Khablenko
 */
public class MethodSignature {

    private static final String METHOD_SIGNATURE_TEMPLATE = "{0} {1}({2} {3})";

    private int id;
    private String methodName;
    private String paramName;
    private String paramType;
    private String returnType;

    public MethodSignature(String methodName, String paramName, String paramType, String returnType) {
        this.methodName = methodName;
        this.paramName = paramName;
        this.paramType = paramType;
        this.returnType = returnType;
    }

    public String toString() {
        return MessageFormat.format(METHOD_SIGNATURE_TEMPLATE, returnType, methodName, paramType, paramName);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    // TODO: Implement hashCode() and equals()
}
