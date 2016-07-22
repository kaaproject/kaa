/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.io.File;

import org.apache.avro.Schema;
import org.apache.commons.io.FileUtils;
import org.apache.flume.Context;
import org.apache.flume.conf.Configurable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.flume.ConfigurationConstants;

import com.google.common.base.Preconditions;

public class AvroSchemaSource implements Configurable, ConfigurationConstants {

    private static final String KAA_ADMIN_REST_API_LOG_SCHEMA = "/kaaAdmin/rest/api/logSchema/";
    private static final String KAA_ADMIN_REST_API_CTL_SCHEMA = "/kaaAdmin/rest/api/CTL/getFlatSchemaByCtlSchemaId?id=";

    public static final String SCHEMA_SOURCE = "flume.avro.schema.source";

    private String schemaSourceType;
    private String kaaRestHost;
    private int kaaRestPort;
    private String kaaRestUser;
    private String kaaRestPassword;
    private String schemaLocalRoot;

    private DefaultHttpClient httpClient;
    private HttpHost restHost;
    private HttpContext httpContext;

    @Override
    public void configure(Context context) {
        schemaSourceType = context.getString(CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE, DEFAULT_AVRO_EVENT_SERIALIZER_SCHEMA_SOURCE);
        if (schemaSourceType.equals(SCHEMA_SOURCE_REST)) {
            kaaRestHost = context.getString(CONFIG_KAA_REST_HOST, DEFAULT_KAA_REST_HOST);
            kaaRestPort = context.getInteger(CONFIG_KAA_REST_PORT, DEFAULT_KAA_REST_PORT);
            kaaRestUser = context.getString(CONFIG_KAA_REST_USER);
            kaaRestPassword = context.getString(CONFIG_KAA_REST_PASSWORD);

            Preconditions.checkArgument(kaaRestUser != null && kaaRestUser.length() > 0,
                    CONFIG_KAA_REST_USER + " must be specified for " + SCHEMA_SOURCE_REST + " avro schema source");
            Preconditions.checkArgument(kaaRestPassword != null && kaaRestPassword.length() > 0,
                    CONFIG_KAA_REST_PASSWORD + " must be specified for " + SCHEMA_SOURCE_REST + " avro schema source");

            initHttpRestClient();
        } else {
            schemaLocalRoot = context.getString(CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT);

            Preconditions.checkArgument(schemaLocalRoot != null && schemaLocalRoot.length() > 0,
                    CONFIG_AVRO_EVENT_SERIALIZER_SCHEMA_LOCAL_ROOT + " must be specified for " + SCHEMA_SOURCE_LOCAL + " avro schema source");
        }
    }

    private void initHttpRestClient() {
        httpClient = new DefaultHttpClient();
        restHost = new HttpHost(kaaRestHost, kaaRestPort, "http");

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(restHost, basicAuth);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(kaaRestHost, kaaRestPort, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(kaaRestUser, kaaRestPassword));

        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        httpContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);

    }

    public Schema loadByKey(KaaSinkKey key) throws Exception {
        Schema schema = null;
        String schemaString = null;
        String logSchema = null;
        if (schemaSourceType.equals(SCHEMA_SOURCE_REST)) {
            HttpGet getRequest = new HttpGet(KAA_ADMIN_REST_API_LOG_SCHEMA + key.getApplicationToken() + "/" + key.getSchemaVersion());
            HttpResponse httpResponse = httpClient.execute(restHost, getRequest, httpContext);
            HttpEntity entity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
                String content = EntityUtils.toString(entity);
                ObjectMapper mapper = new ObjectMapper();
                LogSchemaDto logSchemaDto = mapper.readValue(content, LogSchemaDto.class);
                HttpGet getCtlRequest = new HttpGet(KAA_ADMIN_REST_API_CTL_SCHEMA + logSchemaDto.getCtlSchemaId());
                HttpResponse httpCtlResponse = httpClient.execute(restHost, getCtlRequest, httpContext);
                HttpEntity ctlEntity = httpCtlResponse.getEntity();
                if (httpCtlResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && ctlEntity != null) {
                    String ctlContent = EntityUtils.toString(entity);
                    ObjectMapper ctlMapper = new ObjectMapper();
                    logSchema = ctlMapper.readValue(ctlContent, String.class);
                }
                schemaString = logSchema;
                EntityUtils.consume(entity);
            }
        } else {
            String applicationToken = key.getApplicationToken();
            int version = key.getSchemaVersion();
            String separator = System.getProperty("file.separator");
            File schemaFile = new File(schemaLocalRoot + separator + applicationToken + separator + "schema_v" + version);
            if (schemaFile.exists()) {
                schemaString = FileUtils.readFileToString(schemaFile);
            }
        }
        if (schemaString != null) {
            Schema.Parser parser = new Schema.Parser();
            schema = parser.parse(schemaString);
        }
        return schema;
    }

}
