package org.kaaproject.kaa.demo.iotworld.irrigation;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMSRunner {

    private static final String DEFAULT_USER_NAME = "kaa";
    private static final String DEFAULT_ACCESS_CODE = "IRRIGATION_SYSTEM_ACCESS_CODE";

    private static final Logger LOG = LoggerFactory.getLogger(IMSRunner.class);

    private static KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Raspberry PI 2 controller started");
        LOG.info("--= Press any key to exit =--");

        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");
                try {
                    kaaClient.attachUser(DEFAULT_USER_NAME, "", new UserAttachCallback() {
                        @Override
                        public void onAttachResult(UserAttachResponse arg0) {
                            new IMSController(kaaClient).init();
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Got error during running IMS controller", e);
                }
            }

            @Override
            public void onStopped() {
                LOG.info("Kaa client stopped");
            }
        });

        try {
            kaaClient.setEndpointAccessToken(DEFAULT_ACCESS_CODE);
            kaaClient.start();
            System.in.read();
        } catch (Exception e) {
            LOG.info("Got exception during running kaa client", e);
        }

        kaaClient.stop();
    }
}
