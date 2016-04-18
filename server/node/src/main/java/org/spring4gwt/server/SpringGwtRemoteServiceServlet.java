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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SpringGwtRemoteServiceServlet extends RemoteServiceServlet {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SpringGwtRemoteServiceServlet.class);

    static ThreadLocal<HttpServletRequest> perThreadRequest = 
            new ThreadLocal<HttpServletRequest>();

	@Override
	public void init() {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Spring GWT service exporter deployed");
		}
	}

	@Override
	public String processCall(String payload) throws SerializationException {
		try {
		    perThreadRequest.set(getThreadLocalRequest());
			Object handler = getBean(getThreadLocalRequest());
			RPCRequest rpcRequest = RPC.decodeRequest(payload, handler.getClass(), this);
			onAfterRequestDeserialized(rpcRequest);
			if (LOG.isDebugEnabled()) {
			    LOG.debug("Invoking " + handler.getClass().getName() + "." + rpcRequest.getMethod().getName());
			}
			return RPCHelper.invokeAndEncodeResponse(handler, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest
					.getSerializationPolicy());
		} catch (IncompatibleRemoteServiceException ex) {
			log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
			return RPC.encodeResponseForFailure(null, ex);
		} catch (SerializationException ex) {
		    LOG.error("An SerializationException was thrown while processing this call.", ex);
        	throw ex;
		} finally {
            perThreadRequest.set(null);
        }
	}

    public static HttpServletRequest getRequest() {
        return perThreadRequest.get();
    }
    
    public static void setRequest(HttpServletRequest request) {
        perThreadRequest.set(request);
    }

	/**
	 * Determine Spring bean to handle request based on request URL, e.g. a
	 * request ending in /myService will be handled by bean with name
	 * "myService".
	 * 
	 * @param  request the request
	 * @return handler bean
	 */
	protected Object getBean(HttpServletRequest request) {
		String service = getService(request);
		Object bean = getBean(service);
		if (!(bean instanceof RemoteService)) {
			throw new IllegalArgumentException("Spring bean is not a GWT RemoteService: " + service + " (" + bean + ")");
		}
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Bean for service " + service + " is " + bean);
		}
		return bean;
	}

	/**
	 * Parse the service name from the request URL.
	 * 
	 * @param  request the request
	 * @return bean name
	 */
	protected String getService(HttpServletRequest request) {
		String url = request.getRequestURI();
		String service = url.substring(url.lastIndexOf("/") + 1);
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Service for URL {} is {}", url, service);
		}
		return service;
	}

	/**
	 * Look up a spring bean with the specified name in the current web
	 * application context.
	 * 
	 * @param name
	 *            bean name
	 * @return the bean
	 */
	protected Object getBean(String name) {
		WebApplicationContext applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		if (applicationContext == null) {
			throw new IllegalStateException("No Spring web application context found");
		}
		if (!applicationContext.containsBean(name)) {
			{
				throw new IllegalArgumentException("Spring bean not found: " + name);
			}
		}
		return applicationContext.getBean(name);
	}
}
