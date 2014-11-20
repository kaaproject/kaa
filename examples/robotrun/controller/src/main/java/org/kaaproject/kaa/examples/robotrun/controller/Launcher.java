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

package org.kaaproject.kaa.examples.robotrun.controller;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.profile.AbstractProfileContainer;
import org.kaaproject.kaa.examples.robotrun.controller.Controller.ControllerCallback;
import org.kaaproject.kaa.examples.robotrun.controller.configuration.ConfigurationManager;
import org.kaaproject.kaa.examples.robotrun.controller.configuration.DefaultConfigurationManager;
import org.kaaproject.kaa.examples.robotrun.controller.events.DefaultEventManager;
import org.kaaproject.kaa.examples.robotrun.controller.log.DefaultLogManager;
import org.kaaproject.kaa.examples.robotrun.controller.path.DepthFirstSearchAlgorithm;
import org.kaaproject.kaa.examples.robotrun.controller.path.KaaAlgorithm;
import org.kaaproject.kaa.examples.robotrun.controller.robot.Drivable;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.ErrorCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.StateCallback;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.schema.base.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Launcher implements StateCallback, ControllerCallback, ErrorCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    private final Kaa kaa;
    private final Controller controller;
    private final LauncherCallback callback;

    private String entityName;

    private boolean startPending = false;
    private boolean connected = false;

    public Launcher(Kaa kaa,
            LauncherCallback callback,
            Drivable drivable,
            int startX,
            int startY,
            Direction startDirection,
            String name) {
        this.kaa = kaa;
        this.callback = callback;
        this.entityName = name;
        drivable.registerStateCallback(this);
        drivable.registerErrorCallback(this);
        kaa.getClient().getProfileManager().setProfileContainer(new BasicProfileContainer());
        final Context context = new Context();
        final ConfigurationManager configurationManager = new DefaultConfigurationManager(kaa.getClient());
        configurationManager.setStartPosition(startX, startY, startDirection);
        context.setConfigurationManager(configurationManager);
        context.setEventManager(new DefaultEventManager(kaa.getClient(), EntityType.ROBOT,
            new DefaultEventManager.EventManagerDataProvider() {
                @Override
                public Cell getCurrentCell() {
                    if (context.getCell() == null) {
                        return configurationManager.getStartPosition();
                    } else {
                        return context.getCell();
                    }
                }

                @Override
                public Labyrinth getLabyrinth() {
                    return context.getLabyrinth();
                }

                @Override
                public String getEntityName() {
                    return entityName;
                }
            }
        ));
        context.setLogManager(new DefaultLogManager(kaa.getClient()));
        context.setDrivable(drivable);
        context.setExitSearchAlgorithm(new KaaAlgorithm());
        context.setPathSearchAlgorithm(new DepthFirstSearchAlgorithm());
        controller = new Controller(context, this);
    }

    public void start() {
        try {
            startPending = true;
            kaa.start();
            controller.start();
        } catch (Exception e) {
            LOG.error("Failed to start controller!", e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        controller.stop();
        kaa.stop();
    }

    @Override
    public void onConnected(String deviceName) {
        LOG.info("onConnected, deviceName [{}], startPending [{}], controller.isStarted [{}]",
                deviceName,
                startPending,
                controller.isStarted());
        this.connected = true;
        this.entityName = deviceName;
        if (startPending && controller.isStarted()) {
            startPending = false;
            callback.onLauncherStarted(this.entityName);
        }
    }

    @Override
    public void onDisconnected() {
        LOG.info("onDisconnected, controller.isStarted [{}]", controller.isStarted());
        this.connected = false;
        if (controller.isStarted()) {
            stop();
        }
    }

    @Override
    public void error(Exception e) {
        LOG.error("Robot error:", e);
        callback.onLauncherError(e);
    }

    @Override
    public void onControllerStarted() {
        LOG.info("onControllerStarted, startPending [{}], connected [{}]", startPending, connected);
        if (startPending && connected) {
            startPending = false;
            callback.onLauncherStarted(this.entityName);
        }
    }

    @Override
    public void onControllerStartFailed() {
        LOG.info("onControllerStartFailed");
        startPending = false;
        stop();
    }

    @Override
    public void onControllerStartRun() {
        LOG.info("onControllerStartRun");
        callback.onLauncherStartRun();
    }

    @Override
    public void onControllerStoped() {
        LOG.info("onControllerStoped");
        callback.onLauncherStopped();
    }

    class BasicProfileContainer extends AbstractProfileContainer {

        @Override
        public Profile getProfile() {
            return new Profile();
        }

    }

    public interface LauncherCallback {

        void onLauncherStarted(String entityName);

        void onLauncherError(Exception e);

        void onLauncherStartRun();

        void onLauncherStopped();

    }

}
