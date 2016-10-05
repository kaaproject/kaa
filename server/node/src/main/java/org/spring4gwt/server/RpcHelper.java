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

package org.spring4gwt.server;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RpcHelper {

  private static final Log LOG = LogFactory.getLog(RpcHelper.class);

  private RpcHelper() {
  }

  public static String invokeAndEncodeResponse(Object target,
                                               Method serviceMethod,
                                               Object[] args,
                                               SerializationPolicy serializationPolicy)
      throws SerializationException {
    return invokeAndEncodeResponse(target, serviceMethod, args, serializationPolicy,
        AbstractSerializationStream.DEFAULT_FLAGS);
  }

  /**
   * Invoke the method on targeted object and encode received response.
   *
   * @param target              the target object
   * @param serviceMethod       the service method
   * @param args                the args of method
   * @param serializationPolicy the serialization policy
   * @param flags               the flags
   * @throws SerializationException the serialization exception
   */
  public static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
                                               SerializationPolicy serializationPolicy,
                                               int flags) throws SerializationException {
    if (serviceMethod == null) {
      throw new NullPointerException("serviceMethod");
    }

    if (serializationPolicy == null) {
      throw new NullPointerException("serializationPolicy");
    }

    String responsePayload;
    try {
      Object result = serviceMethod.invoke(target, args);

      responsePayload = RPC.encodeResponseForSuccess(serviceMethod, result,
          serializationPolicy, flags);

    } catch (IllegalAccessException ex) {
      SecurityException securityException =
          new SecurityException(formatIllegalAccessErrorMessage(target, serviceMethod));
      securityException.initCause(ex);
      throw securityException;
    } catch (IllegalArgumentException ex) {
      SecurityException securityException =
          new SecurityException(formatIllegalArgumentErrorMessage(target, serviceMethod, args));
      securityException.initCause(ex);
      throw securityException;
    } catch (InvocationTargetException ex) {
      // Try to encode the caught exception
      //
      Throwable cause = ex.getCause();

      LOG.error("Unexpected exception occured while invoking service method - "
          + serviceMethod.getName(), ex);

      responsePayload = RPC
          .encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags);
    }

    return responsePayload;
  }

  private static String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod,
                                                          Object[] args) {
    StringBuffer sb = new StringBuffer();
    sb.append("Blocked attempt to invoke method '");
    sb.append(getSourceRepresentation(serviceMethod));
    sb.append("'");

    if (target != null) {
      sb.append(" on target '");
      sb.append(printTypeName(target.getClass()));
      sb.append("'");
    }

    sb.append(" with invalid arguments");

    if (args != null && args.length > 0) {
      sb.append(Arrays.asList(args));
    }

    return sb.toString();
  }

  private static String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
    StringBuffer sb = new StringBuffer();
    sb.append("Blocked attempt to access inaccessible method '");
    sb.append(getSourceRepresentation(serviceMethod));
    sb.append("'");

    if (target != null) {
      sb.append(" on target '");
      sb.append(printTypeName(target.getClass()));
      sb.append("'");
    }

    sb.append("; this is either misconfiguration or a hack attempt");

    return sb.toString();
  }

  private static String printTypeName(Class<?> type) {
    // Primitives
    //
    if (type.equals(Integer.TYPE)) {
      return "int";
    } else if (type.equals(Long.TYPE)) {
      return "long";
    } else if (type.equals(Short.TYPE)) {
      return "short";
    } else if (type.equals(Byte.TYPE)) {
      return "byte";
    } else if (type.equals(Character.TYPE)) {
      return "char";
    } else if (type.equals(Boolean.TYPE)) {
      return "boolean";
    } else if (type.equals(Float.TYPE)) {
      return "float";
    } else if (type.equals(Double.TYPE)) {
      return "double";
    }

    // Arrays
    //
    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      return printTypeName(componentType) + "[]";
    }

    // Everything else
    //
    return type.getName().replace('$', '.');
  }

  private static String getSourceRepresentation(Method method) {
    return method.toString().replace('$', '.');
  }


}
