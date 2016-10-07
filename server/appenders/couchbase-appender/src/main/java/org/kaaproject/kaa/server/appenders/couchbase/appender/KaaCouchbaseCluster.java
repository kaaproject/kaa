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

import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.ClusterInfo;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.List;

public class KaaCouchbaseCluster {

  private final List<String> bootstrapHosts;
  private final String bucketName;
  private final String bucketPassword;
  private CouchbaseEnvironment environment;
  private CouchbaseCluster cluster;
  private ClusterInfo clusterInfo;

  /**
   * Instantiates a new Kaa couchbase cluster.
   *
   * @param bootstrapHosts the bootstrap hosts
   * @param bucketName     the bucket name
   * @param bucketPassword the bucket password
   */
  public KaaCouchbaseCluster(List<String> bootstrapHosts, String bucketName,
                             String bucketPassword) {
    this.bootstrapHosts = bootstrapHosts;
    this.bucketName = bucketName;
    this.bucketPassword = bucketPassword;
  }

  /**
   * Create couchbase template.
   *
   * @return the couchbase template
   * @throws Exception the exception
   */
  public CouchbaseTemplate connect() throws Exception {
    environment = DefaultCouchbaseEnvironment.create();
    cluster = CouchbaseCluster.create(environment, bootstrapHosts);
    clusterInfo = cluster.clusterManager(bucketName, bucketPassword).info();
    return new CouchbaseTemplate(
        clusterInfo,
        cluster.openBucket(bucketName, bucketPassword));
  }

  public void disconnect() throws Exception {
    cluster.disconnect();
    environment.shutdownAsync();
  }
}
