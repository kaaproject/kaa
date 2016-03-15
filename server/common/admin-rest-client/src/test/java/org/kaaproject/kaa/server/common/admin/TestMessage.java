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
