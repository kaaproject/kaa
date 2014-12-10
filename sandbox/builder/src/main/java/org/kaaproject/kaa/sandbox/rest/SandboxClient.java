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
package org.kaaproject.kaa.sandbox.rest;

import java.util.List;

import org.apache.http.HttpHost;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.admin.HttpComponentsRequestFactoryBasicAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class SandboxClient {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClient.class);
    
    private RestTemplate restTemplate;
    
    private String url;

    public SandboxClient(String host, int port) {
        restTemplate = new RestTemplate();
        ClientHttpRequestFactory requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(host, port, "http"));
        restTemplate.setRequestFactory(requestFactory);
        url = "http://"+host+":"+port + "/sandbox/rest/api/";
    }
    
    public List<Project> getDemoProjects() throws Exception {
        ParameterizedTypeReference<List<Project>> typeRef = new ParameterizedTypeReference<List<Project>>() {};
        ResponseEntity<List<Project>> entity = restTemplate.exchange(url + "demoProjects", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }
    
    public boolean isProjectBinaryDataExists(String projectId) {
        return restTemplate.getForObject(url + "isProjectDataExists?projectId={projectId}&dataType={dataType}", Boolean.class, projectId, "BINARY");
    }
    
    public String buildProjectBinary(String projectId) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("projectId", projectId);
        request.add("dataType", "BINARY");
        byte[] result = restTemplate.postForObject(url + "buildProjectData", request, byte[].class);
        return new String(result);
    }
            
}
