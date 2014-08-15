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

package org.kaaproject.kaa.server.operations.service.logs.flume.client;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;

import de.flapdoodle.embed.process.io.file.Files;

public abstract class FlumeClientManagerTest {

    private static final String FLUME_TARBALL_URL = "http://kaajenkins.cybervisiontech.com.ua/flume/apache-flume.tar.gz";
    private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    private static final String FLUME_GZIP_FILE_NAME = "apache-flume-bin.gz";
    private static final String TMP_DIR = System.getProperty(TMP_DIR_PROPERTY);
    private static final String FLUME = "flume";
    private static final String FLUME_LOG_FILE = FLUME + ".log";
    private static final String FLUME_LOG_DIR = TMP_DIR + "/" + FLUME;
    protected static final String CONFIG_FILE_PRCCLIENT_TEST = "logs/rpc-client-test.properties";

    protected FlumeAppenderParametersDto parametersDto = null;
    protected byte[] testEventBody = new byte[] { 0, 1, 2, 3, 4, 5 };

    protected StagedInstall install = null;
    protected FlumeClientManager clientManager = null;

    @Before
    public final void setUp() throws Exception {
        downloadFlumeTarball();
        install = StagedInstall.getInstance();
        parametersDto = new FlumeAppenderParametersDto();

    }

    @After
    public final void tearDown() throws Exception {
        if (clientManager != null) {
            clientManager.cleanUp();
        }
        if (install != null && install.isRunning()) {
            install.stopAgent();
        }
    }

    protected File downloadFlumeTarball() throws Exception {
        File flume = new File(TMP_DIR + "/" + FLUME_GZIP_FILE_NAME);
        if (!flume.exists()) {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(FLUME_TARBALL_URL);
            HttpResponse response = client.execute(get);
            InputStream stream = response.getEntity().getContent();
            Files.write(stream, flume);

        }
        System.setProperty(StagedInstall.PROP_PATH_TO_DIST_TARBALL, flume.getAbsolutePath());
        System.setProperty(StagedInstall.ENV_FLUME_LOG_DIR, FLUME_LOG_DIR);
        System.setProperty(StagedInstall.ENV_FLUME_LOG_FILE, FLUME_LOG_FILE);
        return flume;
    }

    protected List<HostInfoDto> generateHosts(int count) {
        List<HostInfoDto> hosts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hosts.add(new HostInfoDto("localhost", 12121 + i, 0 + i));
        }
        return hosts;
    }

}
