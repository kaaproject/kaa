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

package org.kaaproject.kaa.demo.iotworld.citylights.resources;

import static org.kaaproject.kaa.demo.iotworld.citylights.resources.Utils.decodeRfidLog;
import static org.kaaproject.kaa.demo.iotworld.citylights.resources.Utils.decodeTrafficLog;
import static org.kaaproject.kaa.demo.iotworld.citylights.resources.Utils.toTag;
import static org.kaaproject.kaa.demo.iotworld.citylights.resources.Utils.toTags;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.citylights.CityLightsConfiguration;
import org.kaaproject.kaa.demo.iotworld.citylights.ControllerApplication;
import org.kaaproject.kaa.demo.iotworld.citylights.KaaClientConfiguration;
import org.kaaproject.kaa.demo.iotworld.citylights.StreetLightsConfiguration;
import org.kaaproject.kaa.demo.iotworld.citylights.TrafficLightsConfiguration;
import org.kaaproject.kaa.demo.iotworld.citylights.client.KaaRestController;
import org.kaaproject.kaa.demo.iotworld.citylights.state.StreetLightState;
import org.kaaproject.kaa.demo.iotworld.citylights.state.TrafficLightState;
import org.kaaproject.kaa.demo.iotworld.connectedcar.RfidLog;
import org.kaaproject.kaa.demo.iotworld.lights.traffic.EventType;
import org.kaaproject.kaa.demo.iotworld.lights.traffic.TrafficLightsLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("data")
public class ControllerResource {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerResource.class);

    private static final KaaClient kaaClient = ControllerApplication.getKaaClient();
    private static final TrafficLightState trafficState = new TrafficLightState();
    private static final StreetLightState streetState = new StreetLightState();
    private static final boolean ALLOW = true;
    private static final boolean DISALLOW = false;

    @PUT
    @Path("car")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response onCarLog(String reportBody) {
        final RfidLog report = decodeRfidLog(reportBody);
        if (report == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                processRfidLog(report);
            }
        });
        t.start();

        return Response.noContent().build();
    }

    @PUT
    @Path("pedestrian")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response onPedestrainLog(String reportBody) {
        final TrafficLightsLog report = decodeTrafficLog(reportBody);
        if (report == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                processTrafficLog(report);
            }
        });
        t.start();
        return Response.noContent().build();
    }

    private static synchronized void processRfidLog(RfidLog log) {
        String tag = toTag(log.getTag());
        LOG.info("processing tag: {}", tag);
        CityLightsConfiguration configuration = kaaClient.getConfiguration();
        if (!isValid(configuration)) {
            LOG.warn("Application configuration is not valid yet");
        }
        processTraficLights(tag, configuration);
        processStreetLights(tag, configuration);
    }

    private static synchronized void processTrafficLog(TrafficLightsLog log) {
        LOG.info("processing traffic lights log: {}", log);
        if (log.getEventType() != EventType.BUTTON) {
            LOG.warn("unsupported event type {}", log.getEventType());
            return;
        }
        processPedestrianWalkRequest();
    }

    private static void processTraficLights(String tag, CityLightsConfiguration configuration) {
        TrafficLightsConfiguration trafficConfig = configuration.getTrafficLightsConfiguration();
        Set<String> allowTags = toTags(trafficConfig.getMainRoadAllowTags());
        if (allowTags.contains(tag)) {
            LOG.info("This is trafficLights allow tag!");
            if (!trafficState.isCarPending()) {
                trafficState.setCarPending(true);
                if (!trafficState.isPedestrianPending()) {
                    updateTrafficLightsState(configuration.getKaaClientConfiguration(), ALLOW);
                } else {
                    LOG.info("Can't allow car movement yet. Pedestrian is on his way!");
                }
            } else {
                LOG.info("Traffic state is already in \"car pending\" state!");
            }
        } else {
            LOG.info("Car is outside the traffic light zone!");
            if (trafficState.isCarPending()) {
                updateTrafficLightsState(configuration.getKaaClientConfiguration(), DISALLOW);
                trafficState.setCarPending(false);
                if (trafficState.isPedestrianPending()) {
                    processPedestrianWalkRequest();
                } else {
                    startSwitchTimer();
                }
            }
        }
    }

    private static void processPedestrianWalkRequest() {
        LOG.info("processing pedestrian walk request");
        final CityLightsConfiguration configuration = kaaClient.getConfiguration();
        trafficState.setPedestrianPending(true);
        if (!trafficState.isCarPending()) {
            updateTrafficLightsState(configuration.getKaaClientConfiguration(), DISALLOW);
            ControllerApplication.getExecutor().schedule(new Runnable() {

                @Override
                public void run() {
                    LOG.info("processing pedestrian walk timeout");
                    trafficState.setPedestrianPending(false);
                    updateTrafficLightsState(configuration.getKaaClientConfiguration(), ALLOW);
                    if (trafficState.isCarPending()) {
                        LOG.info("Controller waiting for car to complete her move");
                    } else {
                        startSwitchTimer();
                    }
                }

            }, configuration.getTrafficLightsConfiguration().getPedestrianWalkDuration(), TimeUnit.SECONDS);
        } else {
            LOG.info("Traffic state is already in \"car pending\" state!");
        }
    }

    public synchronized static void startSwitchTimer() {
        LOG.info("Starting switch timer!");
        if (trafficState.isCarPending() || trafficState.isPedestrianPending()) {
            LOG.warn("Can't start switch timer! Car pending {}, pedestrian pending {}", trafficState.isCarPending(),
                    trafficState.isPedestrianPending());
            return;
        }
        CityLightsConfiguration configuration = kaaClient.getConfiguration();
        if (trafficState.isMainRoad()) {
            ControllerApplication.getExecutor().schedule(createTimerRunnable(DISALLOW),
                    configuration.getTrafficLightsConfiguration().getAllowStateDuration(), TimeUnit.SECONDS);
        } else {
            ControllerApplication.getExecutor().schedule(createTimerRunnable(ALLOW),
                    configuration.getTrafficLightsConfiguration().getDisallowStateDuration(), TimeUnit.SECONDS);
        }
    }

    private static void processStreetLights(String tag, CityLightsConfiguration configuration) {
        List<StreetLightsConfiguration> streetLightsConfigList = configuration.getStreetLightsConfiguration();
        if (streetState.getZoneId() == null) {
            for (StreetLightsConfiguration streetLightsConfig : streetLightsConfigList) {
                updateStreetLightsState(configuration.getKaaClientConfiguration(), streetLightsConfig.getZoneId(), false);
            }
        }
        boolean found = false;
        for (StreetLightsConfiguration streetLightsConfig : streetLightsConfigList) {
            int zoneId = streetLightsConfig.getZoneId();
            LOG.info("processing zone {}", zoneId);
            Set<String> zoneTags = toTags(streetLightsConfig.getTags());
            if (zoneTags.contains(tag)) {
                LOG.info("car is in zone {}", zoneId);
                found = true;
                if (streetState.getZoneId() != null) {
                    if (streetState.getZoneId().intValue() == zoneId) {
                        LOG.info("this is the same zone {}. No update needed", zoneId);
                        continue;
                    }
                    updateStreetLightsState(configuration.getKaaClientConfiguration(), streetState.getZoneId(), false);
                }
                updateStreetLightsState(configuration.getKaaClientConfiguration(), zoneId, true);
            }
        }
        if (!found) {
            if (streetState.getZoneId() != null) {
                LOG.info("car left zone {}.", streetState.getZoneId());
                updateStreetLightsState(configuration.getKaaClientConfiguration(), streetState.getZoneId(), false);
                streetState.setZoneId(null);
            }
        }
    }

    private static Runnable createTimerRunnable(final boolean allow) {
        return new Runnable() {

            @Override
            public void run() {
                LOG.info("processing switch timer");
                if (trafficState.isCarPending() || trafficState.isPedestrianPending()) {
                    LOG.warn("Can't process switch timer! Car pending {}, pedestrian pending {}", trafficState.isCarPending(),
                            trafficState.isPedestrianPending());
                    return;
                }
                CityLightsConfiguration configuration = kaaClient.getConfiguration();
                updateTrafficLightsState(configuration.getKaaClientConfiguration(), allow);
                startSwitchTimer();
            }
        };
    }

    private static void updateStreetLightsState(KaaClientConfiguration configuration, int zoneId, boolean on) {
        try {
            new KaaRestController(configuration).updateStreetLights(zoneId, on);
            if (on) {
                streetState.setZoneId(zoneId);
            }
            LOG.info("Updated street state to {} for zone {}", on ? "ON" : "OFF", zoneId);
        } catch (Exception e) {
            LOG.error("Failed to update street lights state for zone {}", zoneId, e);
        }
    }

    private static void updateTrafficLightsState(KaaClientConfiguration configuration, boolean allow) {
        try {
            new KaaRestController(configuration).updateTrafficLights(allow);
            trafficState.setMainRoad(allow);
            LOG.info("Updated traffic state to {}", allow ? "ALLOW" : "DISALLOW");
        } catch (Exception e) {
            LOG.error("Failed to update traffic lights state", e);
        }
    }

    private static boolean isValid(CityLightsConfiguration configuration) {
        // simple validation of configuration
        return !configuration.getKaaClientConfiguration().getHost().isEmpty();
    }
}
