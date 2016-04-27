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

package org.kaaproject.kaa.server.common.admin;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.web.client.HttpStatusCodeException;

public class TestMessage {

    public String getMessage(KaaRestTemplate kaaRestTemplate, TestHttpMethods httpMethods) {
        String result;
        try {
            switch (httpMethods){
                case GET:{
                    String httpResult = kaaRestTemplate.getForObject("http://localhost:8080/kaaTest", String.class);
                    result = "Message SUCCESS result: " + httpResult;
                    break;
                }
                case PUT:{
                    String putText = "putText";
                    kaaRestTemplate.put("http://localhost:8080/kaaTest/put", putText);
                    result = "Result SUCCESS" + putText;
                    break;
                }
                case DELETE:{
                    kaaRestTemplate.delete("http://localhost:8080/kaaTest/delete");
                    result = "Result SUCCESS";
                    break;
                }
                case POST:{
                    String httpResult = kaaRestTemplate.postForObject("http://localhost:8080/kaaTest/post", new String("postText"), String.class);
                    result = "Message SUCCESS result: " + httpResult;
                    break;
                }
                default:{
                    result = "ERROR";
                }
            }

        } catch (HttpStatusCodeException e) {
            result = "Get FAILED with HttpStatusCode: " + e.getStatusCode() + "|" + e.getStatusText();
        } catch (RuntimeException e) {
            result = "Get FAILED\n" + ExceptionUtils.getFullStackTrace(e);
        }
        return result;
    }

}
