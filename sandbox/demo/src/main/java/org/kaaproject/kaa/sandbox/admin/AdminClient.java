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
package org.kaaproject.kaa.sandbox.admin;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class AdminClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);
    
    private RestTemplate restTemplate;
    
    private String url;
    
    public AdminClient(String host, int port) {
        restTemplate = new RestTemplate();
        ClientHttpRequestFactory requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(host, port, "http"));
        restTemplate.setRequestFactory(requestFactory);
        url = "http://"+host+":"+port + "/kaaAdmin/rest/api/";
    }
    
    public AuthResultDto checkAuth() throws Exception {
        return restTemplate.getForObject(url + "auth/checkAuth", AuthResultDto.class);
    }
    
    public void createKaaAdmin(String username, String password) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("password", password);
        restTemplate.postForObject(url + "auth/createKaaAdmin", params, Void.class);
    }
    
    public void login(String username, String password) {
        HttpComponentsRequestFactoryBasicAuth requestFactory =
                (HttpComponentsRequestFactoryBasicAuth) restTemplate.getRequestFactory();
        requestFactory.setCredentials(username, password);
    }
    
    public void clearCredentials() {
        HttpComponentsRequestFactoryBasicAuth requestFactory =
                (HttpComponentsRequestFactoryBasicAuth) restTemplate.getRequestFactory();
        requestFactory.getCredentialsProvider().clear();
    }
    
    public ResultCode changePassword(String username, String oldPassword, String newPassword) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("oldPassword", oldPassword);
        params.add("newPassword", newPassword);
        return restTemplate.postForObject(url + "auth/changePassword", params, ResultCode.class);
    }
    
    public TenantUserDto editTenant(TenantUserDto tenant) throws Exception {
        return restTemplate.postForObject(url + "tenant", tenant, TenantUserDto.class);
    }
    
    public List<TenantUserDto> getTenants() throws Exception {
        ParameterizedTypeReference<List<TenantUserDto>> typeRef = new ParameterizedTypeReference<List<TenantUserDto>>() {};
        ResponseEntity<List<TenantUserDto>> entity = restTemplate.exchange(url + "tenants", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }
    
    public ApplicationDto editApplication(ApplicationDto application) throws Exception {
        return restTemplate.postForObject(url + "application", application, ApplicationDto.class);
    }
    
    public List<ApplicationDto> getApplications() throws Exception {
        ParameterizedTypeReference<List<ApplicationDto>> typeRef = new ParameterizedTypeReference<List<ApplicationDto>>() {};
        ResponseEntity<List<ApplicationDto>> entity = restTemplate.exchange(url + "applications", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }
    
    public UserDto editUser(UserDto user) throws Exception {
        return restTemplate.postForObject(url + "user", user, UserDto.class);
    }
    
    public List<UserDto> getUsers() throws Exception {
        ParameterizedTypeReference<List<UserDto>> typeRef = new ParameterizedTypeReference<List<UserDto>>() {};
        ResponseEntity<List<UserDto>> entity = restTemplate.exchange(url + "users", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws Exception {
        return restTemplate.postForObject(url + "eventClassFamily", eventClassFamily, EventClassFamilyDto.class);
    }
    
    public void addEventClassFamilySchema(String eventClassFamilyId, String schemaResource) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("eventClassFamilyId", eventClassFamilyId);
        params.add("file", getFileResource(schemaResource));
        restTemplate.postForLocation(url + "addEventClassFamilySchema", params);
    }
    
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId,
            int version,
            EventClassType type) throws Exception {
        ParameterizedTypeReference<List<EventClassDto>> typeRef = new ParameterizedTypeReference<List<EventClassDto>>() {};
        ResponseEntity<List<EventClassDto>> entity = 
                restTemplate.exchange(
                        url + "eventClasses?eventClassFamilyId={eventClassFamilyId}&version={version}&type={type}", 
                        HttpMethod.GET, null, typeRef, eventClassFamilyId, version, type);
        return entity.getBody();
    }
    
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws Exception {
        return restTemplate.postForObject(url + "applicationEventMap", applicationEventFamilyMap, ApplicationEventFamilyMapDto.class);
    }
    
    public void downloadSdk(SdkKey key, String destination) throws Exception {
        FileResponseExtractor extractor = new FileResponseExtractor( new File(destination));
        final List<MediaType> mediaTypes = Arrays.asList(MediaType.APPLICATION_JSON, 
                MediaType.valueOf("application/*+json"));
        final HttpEntity<SdkKey> requestEntity = new HttpEntity<>(key);
        RequestCallback request = new RequestCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void doWithRequest(ClientHttpRequest httpRequest)
                    throws IOException {
                
                httpRequest.getHeaders().setAccept(mediaTypes);
                Object requestBody = requestEntity.getBody();
                Class<?> requestType = requestBody.getClass();
                HttpHeaders requestHeaders = requestEntity.getHeaders();
                MediaType requestContentType = requestHeaders.getContentType();

                for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
                    if (messageConverter.canWrite(requestType, requestContentType)) {
                        if (!requestHeaders.isEmpty()) {
                            httpRequest.getHeaders().putAll(requestHeaders);
                        }
                        ((HttpMessageConverter<Object>) messageConverter).write(
                                requestBody, requestContentType, httpRequest);
                        return;
                    }
                }
            }
        };
        restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
        logger.info("Downloaded sdk to file '{}'", extractor.getDestFile());
    }
    
    public FileData downloadSdk(SdkKey key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        final List<MediaType> mediaTypes = Arrays.asList(MediaType.APPLICATION_JSON, 
                MediaType.valueOf("application/*+json"));
        final HttpEntity<SdkKey> requestEntity = new HttpEntity<>(key);
        RequestCallback request = new RequestCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void doWithRequest(ClientHttpRequest httpRequest)
                    throws IOException {
                
                httpRequest.getHeaders().setAccept(mediaTypes);
                Object requestBody = requestEntity.getBody();
                Class<?> requestType = requestBody.getClass();
                HttpHeaders requestHeaders = requestEntity.getHeaders();
                MediaType requestContentType = requestHeaders.getContentType();

                for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
                    if (messageConverter.canWrite(requestType, requestContentType)) {
                        if (!requestHeaders.isEmpty()) {
                            httpRequest.getHeaders().putAll(requestHeaders);
                        }
                        ((HttpMessageConverter<Object>) messageConverter).write(
                                requestBody, requestContentType, httpRequest);
                        return;
                    }
                }
            }
        };
        FileData data = restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
        return data;
    }
    
    private static final Pattern fileNamePattern = Pattern.compile("^(.+?)filename=\"(.+?)\"");
    
    private static class FileResponseExtractor implements ResponseExtractor<Object>
    {
        
        private final File destDir;
        private File destFile;
     
        private FileResponseExtractor ( File destDir )
        {
            this.destDir = destDir;
        }
        
        public File getDestFile() {
            return destFile;
        }
     
        @Override
        public Object extractData ( ClientHttpResponse response ) throws IOException
        {
            String fileName = "";
            String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
            if (StringUtils.isNotBlank(contentDisposition)) {
                Matcher m = fileNamePattern.matcher(contentDisposition);
                if (m.matches()) {
                    fileName = m.group(2);
                }
            }
            if (StringUtils.isBlank(fileName)) {
                fileName = "downloaded-" + System.currentTimeMillis();
            }
            destFile = new File(destDir, fileName);
            
            InputStream  is = response.getBody();
            OutputStream os = new BufferedOutputStream( new FileOutputStream(destFile));
     
            IOUtils.copyLarge( is, os );
            IOUtils.closeQuietly( is );
            IOUtils.closeQuietly( os );
     
            return null;
        }
    }
    
    private static class FileDataResponseExtractor implements ResponseExtractor<FileData>
    {
        
        private FileDataResponseExtractor ()
        {}
        
        @Override
        public FileData extractData ( ClientHttpResponse response ) throws IOException
        {
            String fileName = "";
            String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
            if (StringUtils.isNotBlank(contentDisposition)) {
                Matcher m = fileNamePattern.matcher(contentDisposition);
                if (m.matches()) {
                    fileName = m.group(2);
                }
            }
            if (StringUtils.isBlank(fileName)) {
                fileName = "downloaded-" + System.currentTimeMillis();
            }
            
            
            InputStream  is = response.getBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = new BufferedOutputStream( baos);
     
            IOUtils.copyLarge( is, os );
            IOUtils.closeQuietly( is );
            IOUtils.closeQuietly( os );

            FileData data = new FileData();
            data.setFileName(fileName);
            data.setFileData(baos.toByteArray());

            return data;
        }
    }


    private static ByteArrayResource getFileResource(final String resource) throws IOException {
        byte[] data = FileUtils.readResourceBytes(resource);
        ByteArrayResource bar = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return resource;
            }
        };
        return bar;
    }
    
    
}
