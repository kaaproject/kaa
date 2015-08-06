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

package org.kaaproject.kaa.demo.iotworld.connectedcar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.client.util.CommonsBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarEmulator {

    private static final Logger LOG = LoggerFactory.getLogger(CarEmulator.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void main(String[] args) {
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        final Set<String> homeTags = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        final Set<String> nearTags = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        // Set a custom strategy for uploading logs.
        // The default strategy uploads logs after either a threshold logs count 
        // or a threshold logs size has been reached.
        // The following custom strategy uploads every log record as soon as it is created.
        kaaClient.setLogUploadStrategy(new DefaultLogUploadStrategy() {
            @Override
            public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
                return status.getRecordCount() >= 1 ? LogUploadStrategyDecision.UPLOAD : LogUploadStrategyDecision.NOOP;
            }
            
            @Override
            public void onFailure(org.kaaproject.kaa.client.logging.LogFailoverCommand controller, org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode code){
                LOG.info("Log upload failed due to {}", code);
//                controller.retryLogUpload(30000);
            }
        });
        
        applyConfiguration(homeTags, nearTags, kaaClient.getConfiguration());
        
        // will always fetch latest configuration becouse we don't store it anywhere;
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(GeofancingConfiguration conf) {
                applyConfiguration(homeTags, nearTags, conf);
            }
        });

        kaaClient.start();

        try {
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter tag");
                String tag = br.readLine();
                if (!tag.equalsIgnoreCase("exit")) {
                    LOG.info("User entered tag : {}", tag);
                    try {
                        byte[] tagData = toByteArray(tag);
                        kaaClient.addLogRecord(new RfidLog(ByteBuffer.wrap(tagData)));
                        if(homeTags.contains(tag)){
                            LOG.info("This is HOME tag. TODO: Broadcast event!");
                        }
                        if(nearTags.contains(tag)){
                            LOG.info("This is NEAR tag. TODO: Broadcast event!");
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to decode tag: {}", e.getMessage(), e);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }
    }

    private static List<String> toTags(List<ByteBuffer> buffers) {
        List<String> result = new ArrayList<String>(buffers.size());
        for (ByteBuffer buf : buffers) {
            byte[] data = CommonsBase64.getInstance().encodeBase64(toByteArray(buf));
            result.add(new String(data, UTF8));
        }
        return result;
    }

    private static byte[] toByteArray(String string) {
        return string.getBytes(UTF8);
    }

    private static byte[] toByteArray(ByteBuffer buffer) {
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    private static void applyConfiguration(final Set<String> homeTags, final Set<String> nearTags, GeofancingConfiguration conf) {
        homeTags.clear();
        homeTags.addAll(toTags(conf.getHomeTags()));
        nearTags.clear();
        nearTags.addAll(toTags(conf.getNearTags()));
    }
}
