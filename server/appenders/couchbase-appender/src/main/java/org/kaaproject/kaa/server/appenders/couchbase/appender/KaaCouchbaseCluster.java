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
package org.kaaproject.kaa.server.appenders.couchbase.appender;

import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.List;

public class KaaCouchbaseCluster extends AbstractCouchbaseConfiguration {

    private final List<String> bootstrapHosts;
    private final String bucketName;
    private final String bucketPassword;

    public KaaCouchbaseCluster(List<String> bootstrapHosts, String bucketName, String bucketPassword) {
        this.bootstrapHosts = bootstrapHosts;
        this.bucketName = bucketName;
        this.bucketPassword = bucketPassword;
    }

    @Override
    protected List<String> getBootstrapHosts() {
        return bootstrapHosts;
    }

    @Override
    protected String getBucketName() {
        return bucketName;
    }

    @Override
    protected String getBucketPassword() {
        return bucketPassword;
    }

    public CouchbaseTemplate createTemplate() throws Exception {
        CouchbaseTemplate template = new CouchbaseTemplate(
                couchbaseClusterInfo(),
                couchbaseCluster().openBucket(getBucketName(), getBucketPassword()));
        return template;
    }

    public void disconnect() throws Exception {
        couchbaseCluster().disconnect();
    }
}
