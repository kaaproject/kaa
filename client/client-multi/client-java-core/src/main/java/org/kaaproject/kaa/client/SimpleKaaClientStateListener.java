package org.kaaproject.kaa.client;

import org.kaaproject.kaa.client.exceptions.KaaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleKaaClientStateListener implements KaaClientStateListener {

    private static final Logger LOG = LoggerFactory.getLogger(KaaClientStateListener.class);
    
    @Override
    public void onStarted() {
        LOG.info("Kaa client started");
    }

    @Override
    public void onStartFailure(KaaException exception) {
        LOG.info("Kaa client startup failure", exception);
    }

    @Override
    public void onPaused() {
        LOG.info("Kaa client paused");
    }

    @Override
    public void onPauseFailure(KaaException exception) {
        LOG.info("Kaa client pause failure", exception);
    }

    @Override
    public void onResume() {
        LOG.info("Kaa client resumed");
    }

    @Override
    public void onResumeFailure(KaaException exception) {
        LOG.info("Kaa client resume failure", exception);
    }

    @Override
    public void onStopped() {
        LOG.info("Kaa client stopped");
    }

    @Override
    public void onStopFailure(KaaException exception) {
        LOG.info("Kaa client stop failure", exception);
    }

}
