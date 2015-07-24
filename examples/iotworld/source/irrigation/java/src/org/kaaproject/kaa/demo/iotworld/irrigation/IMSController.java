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
package org.kaaproject.kaa.demo.iotworld.irrigation;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.demo.iotworld.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.IrrigationEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.irrigation.callback.IrrigationCallback;
import org.kaaproject.kaa.demo.iotworld.irrigation.gpio.GpioManager;
import org.kaaproject.kaa.demo.iotworld.irrigation.gpio.GpioManagerImpl;
import org.kaaproject.kaa.demo.iotworld.irrigation.web.MiniWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMSController implements DeviceEventClassFamily.Listener, IrrigationEventClassFamily.Listener, AttachEndpointToUserCallback,
        DetachEndpointFromUserCallback, OnAttachEndpointOperationCallback {

    private static final Logger LOG = LoggerFactory.getLogger(IMSController.class);
    private static final boolean FORCE_STOP_PREVIOUS = true;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final GpioManager gpioManager;

    private final KaaClient kaaClient;
    private final IrrigationConfiguration configuration = IrrigationConfiguration.getInstance();

    private IrrigationEventClassFamily irrigation;
    private DeviceEventClassFamily devices;
    private ScheduledFuture<?> scheduledFuture;
    private MiniWebServer miniWebServer = null;

    private volatile IrrigationStateHolder stateHolder;

    private Set<String> subscriptedDevices = new ConcurrentHashSet<>();

    public IMSController(KaaClient client) {
        this.kaaClient = client;
        this.stateHolder = new IrrigationStateHolder();
        this.gpioManager = new GpioManagerImpl(stateHolder);
        this.miniWebServer = new MiniWebServer(configuration.getWebPort());
        initEventConfigurations(kaaClient);
    }

    @Override
    public void onAttach(SyncResponseResultType result, EndpointKeyHash resultContext) {
        LOG.info("Attached to user by {} with result: {}", resultContext, result);
        if (SyncResponseResultType.SUCCESS.equals(result)) {
            configuration.onAttachtoUser();
            scheduleTimeTaskNow(stateHolder.getIrrigationIntervalMs(), new IrrigationCallbackImpl());
        } else {
            LOG.warn("Recived unsuccessful onAttach callback {}", result);
        }
    }

    @Override
    public void onEvent(IrrigationControlRequest request, final String source) {
        LOG.info("Received irrigation control request from {}", source);
        long newInterval = TimeUnit.SECONDS.toMillis(request.getIrrigationIntervalSec());

        long timeToIrrigation = 0;
        if (scheduledFuture != null && (!scheduledFuture.isCancelled() || scheduledFuture.isDone())) {
            timeToIrrigation = scheduledFuture.getDelay(TimeUnit.MILLISECONDS);
        }

        long newWateringTime = stateHolder.getLastWateringTime() + newInterval;
        if (newWateringTime > timeToIrrigation) {
            stateHolder.setTimeToNextIrrigationMs(newWateringTime - System.currentTimeMillis());
        } else {
            stateHolder.setTimeToNextIrrigationMs(newInterval);
            stateHolder.setLastWateringTime(System.currentTimeMillis());
        }
        stateHolder.setIrrigationIntervalSec(TimeUnit.MILLISECONDS.toSeconds(newInterval));
        scheduleTimeTask(stateHolder.getTimeToNextIrrigationMs(), stateHolder.getIrrigationIntervalMs(), new IrrigationCallbackImpl());

        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending events for {} endpoints", Arrays.toString(subscriptedDevices.toArray()));
        } else {
            LOG.info("Sending events for {} endpoints", subscriptedDevices.size());
        }

        IrrigationStatusUpdate update = stateHolder.getIrrigationStatusUpdate();
        for (String device : subscriptedDevices) {
            if (source.equals(device)) {
                IrrigationStatus status = update.getStatus();
                IrrigationStatus copy = new IrrigationStatus();
                copy.setIgnoreIrrigationIntervalUpdate(true);
                copy.setIsIrrigation(status.getIsIrrigation());
                copy.setIrrigationIntervalSec(status.getIrrigationIntervalSec());
                copy.setMonthlySpentWater(status.getMonthlySpentWater());
                copy.setRemainingWater(status.getRemainingWater());
                copy.setTimeToNextIrrigationMs(status.getTimeToNextIrrigationMs());
                LOG.debug("Sending irrigation status update {} for sending control request device", copy);
                irrigation.sendEvent(new IrrigationStatusUpdate(copy), source);
            } else {
                LOG.debug("Sending irrigation status update {}", update);
                irrigation.sendEvent(update, device);
            }
        }
    }

    @Override
    public void onEvent(StartIrrigationRequest request, String source) {
        LOG.debug("Received event from {} for immediately irrigation.", source);
        if (!stateHolder.isIrrigation()) {
            scheduleTimeTaskNow(stateHolder.getIrrigationIntervalMs(), new IrrigationCallbackImpl());
        } else {
            LOG.warn("Received event from {} for immediately irrigation, but irrigation already in progress", source);
        }
    }

    @Override
    public void onEvent(DeviceInfoRequest request, String source) {
        LOG.debug("Received DeviceInfoRequest event from {}", source);
        DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse(stateHolder.getDeviceInfo());
        LOG.debug("Sending device info response {}", deviceInfoResponse);
        devices.sendEvent(deviceInfoResponse, source);
    }

    @Override
    public void onEvent(DeviceStatusSubscriptionRequest request, String source) {
        LOG.debug("Received DeviceStatusSubscriptionRequest event from {}", source);
        if (irrigation != null) {
            subscriptedDevices.add(source);
            IrrigationStatusUpdate update;
            if (scheduledFuture != null && (!scheduledFuture.isCancelled() || !scheduledFuture.isDone())) {
                update = stateHolder.getIrrigationStatusUpdate(scheduledFuture.getDelay(TimeUnit.MILLISECONDS));
            } else {
                update = stateHolder.getIrrigationStatusUpdate();
            }
            LOG.debug("Sending irrigation status update {}", update);
            irrigation.sendEvent(update, source);
        }
    }

    @Override
    public void onEvent(DeviceChangeNameRequest request, String source) {
        String name = request.getName();
        LOG.info("Received request from {} for changing name {}", source, name);
        stateHolder.updateDeviceName(name);
        devices.sendEvent(new DeviceInfoResponse(stateHolder.getDeviceInfo()), source);
    }

    @Override
    public void onDetachedFromUser(String source) {
        LOG.debug("On detach from user {}", source);
        configuration.onDetachFromUser();
        stopCurrentTask();
    }

    @Override
    public void onAttachedToUser(String arg0, String arg1) {
        LOG.info("Attached to user by {} with result: {}", arg0, arg1);
        long interval = stateHolder.getIrrigationIntervalMs();
        configuration.onAttachtoUser();
        scheduleTimeTask(interval, interval, new IrrigationCallbackImpl());
    }

    private void scheduleTimeTaskNow(long delay, IrrigationCallback callback) {
        scheduleTimeTask(0L, delay, FORCE_STOP_PREVIOUS, callback);
    }

    private void scheduleTimeTask(long initialDelay, long delay, IrrigationCallback callback) {
        scheduleTimeTask(initialDelay, delay, FORCE_STOP_PREVIOUS, callback);
    }

    private void scheduleTimeTask(long initialDelay, long delay, boolean stopRunningTasks, final IrrigationCallback callback) {
        LOG.info("Scheduling task with initial delay {} interval {}", initialDelay, delay);
        stopCurrentTask();
        LOG.debug("Force stoped previous scheduled task {}", scheduledFuture);
        scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.beforeIrrigation();
                    gpioManager.togglePinToHight(configuration.getIrrigationDurationTime());
                    callback.afterIrrigation();
                } catch (Exception e) {
                    LOG.error("Failed to process irrigation!", e);
                    try {
                        gpioManager.togglePinToLow();
                    } catch (Exception e2) {
                        LOG.error("Failed to stop irrigation!", e2);
                    }
                    callback.afterIrrigation();
                }
            }
        }, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    private void initEventConfigurations(KaaClient kaaClient) {
        LOG.debug("Init event configurations.");
        EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        devices = eventFamilyFactory.getDeviceEventClassFamily();
        devices.addListener(this);
        irrigation = eventFamilyFactory.getIrrigationEventClassFamily();
        irrigation.addListener(this);
        kaaClient.setDetachedListener(this);
        kaaClient.setAttachedListener(this);
    }

    private String getAccessToken() {
        String accessToken = kaaClient.getEndpointAccessToken();
        if (accessToken.isEmpty()) {
            LOG.debug("Refresh access token, because current is empty");
            accessToken = kaaClient.refreshEndpointAccessToken();
        }
        LOG.trace("Access token {}", accessToken);
        return accessToken;
    }

    private void stopCurrentTask() {
        if (scheduledFuture != null && (!scheduledFuture.isCancelled() || !scheduledFuture.isDone())) {
            LOG.info("Stopping current task {}", scheduledFuture);
            scheduledFuture.cancel(FORCE_STOP_PREVIOUS);
        }
    }

    class IrrigationCallbackImpl implements IrrigationCallback {

        @Override
        public void beforeIrrigation() {
            IrrigationStatusUpdate update = stateHolder.getBeforeIrrigationStatusUpdate();
            LOG.debug("Sending irrigation status update {} to all", update);
            irrigation.sendEventToAll(update);
        }

        @Override
        public void afterIrrigation() {
            IrrigationStatusUpdate update = stateHolder.getAfterIrrigationStatusUpdate();
            LOG.debug("Sending irrigation status update {} to all", update);
            irrigation.sendEventToAll(update);
        }
    }

    public void init() {
        miniWebServer.start(getAccessToken());
    }
}
