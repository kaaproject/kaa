package org.kaaproject.kaa.demo.iotworld.irrigation;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMSRunner {

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
                    new IMSController(kaaClient).init();
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
            kaaClient.start();
            System.in.read();
        } catch (Exception e) {
            LOG.info("Got exception during running kaa client", e);
        }

        kaaClient.stop();
    }
}
