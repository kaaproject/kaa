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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.kaaproject.kaa.demo.iotworld.device.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrrigationStateHolder {

    private static final boolean IRRIGATION_INPROGRES = true;
    private static final boolean IRRIGATION_IDLE = false;
    private static final boolean RESET_TIME_TO_IRRIGATION = true;
    private static final boolean UPDATE_TIME_TO_IRRIGATION = false;

    private static final Logger LOG = LoggerFactory.getLogger(IrrigationStateHolder.class);
    private final IrrigationConfiguration configuration = IrrigationConfiguration.getInstance();
    private volatile IrrigationStatus irrigationStatus;

    private final DeviceInfo deviceInfo;

    private long lastIrrigationTime = 0L;
    private AtomicInteger flowSensorCount = new AtomicInteger();
    private AtomicInteger totalFlowSensorCount = new AtomicInteger();

    public IrrigationStateHolder() {
        irrigationStatus = new IrrigationStatus();
        irrigationStatus.setMonthlySpentWater(configuration.getDefaultMonthlySpentWater());
        irrigationStatus.setRemainingWater(configuration.getDefaultRemainingWater());
        this.deviceInfo = new DeviceInfo(configuration.getDeviceName(), configuration.getDeviceModelName());
        this.lastIrrigationTime = System.currentTimeMillis();
        setIrrigationIntervalSec(TimeUnit.MILLISECONDS.toSeconds(configuration.getIrrigationIntervalMs()));
    }

    public IrrigationStatusUpdate getIrrigationStatusUpdate() {
        return new IrrigationStatusUpdate(irrigationStatus);
    }

    public IrrigationStatusUpdate getIrrigationStatusUpdate(long timeToIrrigation) {
        irrigationStatus.setTimeToNextIrrigationMs(timeToIrrigation);
        return new IrrigationStatusUpdate(irrigationStatus);
    }

    public long getTimeToNextIrrigationMs() {
        return irrigationStatus.getTimeToNextIrrigationMs();
    }

    public long getIrrigationIntervalMs() {
        return TimeUnit.SECONDS.toMillis(irrigationStatus.getIrrigationIntervalSec());
    }

    public void setIrrigationIntervalSec(long interval) {
        irrigationStatus.setIrrigationIntervalSec((int) interval);
    }

    public void setTimeToNextIrrigationMs(long timeToNextWatering) {
        irrigationStatus.setTimeToNextIrrigationMs(timeToNextWatering);
    }

    public boolean isIrrigation() {
        return irrigationStatus.getIsIrrigation();
    }

    public void onGpioPinDigitalStateChangeEvent() {
        int count = flowSensorCount.incrementAndGet();
        if (count % 100 == 0) {
            LOG.info("Received 100 events from water flow sensor.");
        }
    }

    public long getLastWateringTime() {
        return lastIrrigationTime;
    }

    public void setLastWateringTime(long lastWateringTime) {
        this.lastIrrigationTime = lastWateringTime;
    }

    public IrrigationStatusUpdate getBeforeIrrigationStatusUpdate() {
        LOG.debug("Before irrigation callback");
        updateIrrigationStatus(IRRIGATION_INPROGRES, RESET_TIME_TO_IRRIGATION);
        return new IrrigationStatusUpdate(irrigationStatus);
    }

    public IrrigationStatusUpdate getAfterIrrigationStatusUpdate() {
        LOG.debug("After irrigation callback");
        int currentCount = flowSensorCount.getAndSet(0);
        totalFlowSensorCount.addAndGet(currentCount);
        updateIrrigationStatus(IRRIGATION_IDLE, UPDATE_TIME_TO_IRRIGATION, currentCount);
        lastIrrigationTime = System.currentTimeMillis();
        return new IrrigationStatusUpdate(irrigationStatus);
    }

    private void updateIrrigationStatus(boolean isIrrigation, boolean resetTimeToIrrigation) {
        updateIrrigationStatus(isIrrigation, resetTimeToIrrigation, 0);
    }

    private void updateIrrigationStatus(boolean isIrrigation, boolean resetTimeToIrrigation, int currentCount) {
        irrigationStatus.setIsIrrigation(isIrrigation);
        if (resetTimeToIrrigation) {
            irrigationStatus.setTimeToNextIrrigationMs(0L);
        } else {
            irrigationStatus.setTimeToNextIrrigationMs(getIrrigationIntervalMs());
        }
        irrigationStatus.setMonthlySpentWater(irrigationStatus.getMonthlySpentWater() + currentCount);
        irrigationStatus.setRemainingWater(irrigationStatus.getRemainingWater() - currentCount);
        LOG.trace("Updated irrigation status {}", irrigationStatus);
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void updateDeviceName(String name) {
        deviceInfo.setName(name);
        configuration.storeDeviceName(name);
    }
}
