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

package org.kaaproject.kaa.examples.robotrun.visualization;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationReceiver;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadConfiguration;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.client.profile.AbstractProfileContainer;
import org.kaaproject.kaa.client.schema.storage.SchemaStorage;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.examples.robotrun.controller.events.DefaultEventManager;
import org.kaaproject.kaa.examples.robotrun.controller.events.EventListener;
import org.kaaproject.kaa.examples.robotrun.controller.events.EventManager;
import org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator;
import org.kaaproject.kaa.examples.robotrun.gen.Border;
import org.kaaproject.kaa.examples.robotrun.gen.Borders;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.LabyrinthConverter;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinthConverter;
import org.kaaproject.kaa.examples.robotrun.visualization.EmulatorSetupDialog.EmulatorSetupDialogListener;
import org.kaaproject.kaa.examples.robotrun.visualization.LabyrinthDialog.LabyrinthDialogListener;
import org.kaaproject.kaa.examples.robotrun.visualization.emulator.EmulatorManager;
import org.kaaproject.kaa.examples.robotrun.visualization.emulator.EmulatorManager.EmulatorManagerCallback;
import org.kaaproject.kaa.schema.base.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context implements ConfigurationReceiver, EventListener, EmulatorManagerCallback {
    
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);
    
    private static final String CONFIG_STORAGE_FILE = "desktopConfig.data";
    private static final String CONFIG_SCHEMA_STORAGE_FILE = "desktopConfig.schema";

    private ContextInitCallback callback;
    
    private Kaa kaa;
    private RestEngine restEngine;
    private EmulatorManager emulatorManager;
    
    private Labyrinth labyrinth;
    private Map<String, RobotPosition> robotPositions;
    
    private LabyrinthConverter labyrinthConverter;
    private EventManager eventManager;
    
    private Map<Cell, Direction> emulatorRobots;
    private boolean emulatorSetupPending = false;
    
    private LabyrinthDialog emulatorLabyrinthDialog;
    
    
    private List<ContextListener> contextListeners = new ArrayList<>();
    private List<RobotRunListener> robotRunListeners = new ArrayList<>();
    
    public Context() {
    }
    
    public void init(ContextInitCallback callback)  {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stopRun();
                if (kaa != null) {
                    kaa.stop();
                }
            }
        }));
        this.callback = callback;
        emulatorManager = new EmulatorManager(this, true);

        try {
            kaa = new KaaDesktop();
        } catch (Exception e) {
            LOG.error("Failed to create instance of desktop Kaa!", e);
            showErrorDialog("Failed to create instance of desktop Kaa!", true);
        }
        KaaClient client = kaa.getClient();
        String appToken = null;
        try {
            Field propertiesField = AbstractKaaClient.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            KaaClientProperties props = (KaaClientProperties)propertiesField.get(client);
            appToken = props.getApplicationToken();
        } catch (Exception e) {
            LOG.error("Failed to get client properties!", e);
            showErrorDialog("Failed to get client properties!", true);
        }
        
        client.getProfileManager().setProfileContainer(new BasicProfileContainer());
        client.getLogCollector().setUploadStrategy(new BorderLogUploadStrategy());
        client.getConfigurationManager().subscribeForConfigurationUpdates(this);

        labyrinthConverter = new BasicLabyrinthConverter();
        emulatorRobots = new HashMap<>();
        CommonRecord configurationRecord = client.getConfigurationManager().getConfiguration();
        labyrinth = labyrinthConverter.convert(null, configurationRecord);
        eventManager = new DefaultEventManager(client, EntityType.DESKTOP, 
        new DefaultEventManager.EventManagerDataProvider() {
            @Override
            public Cell getCurrentCell() {
                return null;
            }
            
            @Override
            public Labyrinth getLabyrinth() {
                return labyrinth;
            }
            
            @Override
            public String getEntityName() {
                return "visualizator";
            }
        });

        try {
            client.getSchemaPersistenceManager().setSchemaStorage(new BasicSchemaStorage());
        } catch (Exception e) {
            LOG.error("Failed to set schema storage!", e);
            showErrorDialog("Failed to set schema storage!", true);
        }
        
        try {
            client.getConfigurationPersistenceManager().setConfigurationStorage(new BasicConfigurationStorage());
        } catch (Exception e) {
            LOG.error("Failed to set configuration storage!", e);
            showErrorDialog("Failed to set configuration storage!", true);
        }
        
        eventManager.addListener(this);
        
        robotPositions = eventManager.getRobotPositions();
        
        try {
            kaa.start();
        } catch (IOException | TransportException e) {
            LOG.error("Failed to start Kaa!", e);
            showErrorDialog("Failed to start Kaa!", true);
        }
        restEngine = new RestEngine(appToken);
        try {
            restEngine.init();
        } catch (Exception e) {
            LOG.error("Failed to init rest engine!", e);
            showErrorDialog("Failed to init rest engine!", true);
        }
        eventManager.init();
    }
    
    public Labyrinth getLabyrinth() {
        return labyrinth;
    }
    
    public Map<String, RobotPosition> getRobotPositions() {
        return robotPositions;
    }
    
    public Map<Cell, Direction> getEmulatorRobots() {
        return emulatorRobots;
    }
    
    public void createLabirynth(int width, int height) {
        try {
            this.fireFinished();
            stopEmulator();
            restEngine.createLabyrinth(width, height);
        } catch (Throwable e) {
            LOG.error("Failed to create labirynth!", e);
            showErrorDialog("Failed to create labirynth!", false);
        }
    }
    
    public void resetLabyrinth() {
        try {
            this.fireFinished();
            stopEmulator();
            restEngine.createLabyrinth(labyrinth.getWidth(), labyrinth.getHeight());
            labyrinth.clearDeadends();
        } catch (Throwable e) {
            LOG.error("Failed to create labirynth!", e);
            showErrorDialog("Failed to create labirynth!", false);
        }
    }
    
    public void updateCell(int x, int y, Direction side, BorderType type) {
        Borders borders = new Borders();
        List<Border> changedBorders = new ArrayList<Border>();
        Border border = new Border();
        border.setX((side == Direction.EAST) ? x+1 : x);
        border.setY((side == Direction.SOUTH) ? y+1 : y);
        switch (type) {
        case FREE:
            border.setType(org.kaaproject.kaa.examples.robotrun.gen.BorderType.FREE);
            break;
        case SOLID:
            border.setType(org.kaaproject.kaa.examples.robotrun.gen.BorderType.SOLID);
            break;
        case UNKNOWN:
            border.setType(org.kaaproject.kaa.examples.robotrun.gen.BorderType.UNKNOWN);
            break;
        }
        changedBorders.add(border);
        
        if (side == Direction.WEST || side == Direction.EAST) {
            borders.setVBorders(changedBorders);
            borders.setHBorders(Collections.<Border>emptyList());
        }
        else {
            borders.setHBorders(changedBorders);
            borders.setVBorders(Collections.<Border>emptyList());
        }
        try {
            kaa.getClient().getLogCollector().addLogRecord(borders);
        } catch (IOException e) {
            LOG.error("Failed to add log record!", e);
        }
    }
    
    public void startRun() {
        eventManager.startRun();
        fireStartRun();
    }
    
    public void stopRun() {
        fireFinished();
        stopEmulator();
    }
    
    public void showEmulatorSetupDialog(Component parent) {
        EmulatorSetupDialog.showEmulatorSetupDialog(null, this, new EmulatorSetupDialogListener() {
            
            @Override
            public void onEmulatorSetupDialogOk(Map<Cell, Direction> robots) {
                setupEmulator(robots);
            }
            
            @Override
            public void onEmulatorSetupDialogCancel() {
            }
        });
    }
    
    private void setupEmulator(Map<Cell, Direction> robots) {
        this.emulatorRobots = robots;
        emulatorSetupPending = true;
        eventManager.resetEndpoints();
    }
    
    public void stopEmulator() {
        emulatorManager.stopEmulators();
    }
    
    public boolean isEmulatorStarted() {
        return emulatorManager.isStarted();
    }
    
    public void showEmulatedLabyrinthDialog(Component parent, LabyrinthDialogListener listener) {
        Labyrinth labyrinth = EmulatorManager.loadLabyrinth();
        if (labyrinth != null) {
            if (emulatorLabyrinthDialog != null && emulatorLabyrinthDialog.isVisible()) {
                emulatorLabyrinthDialog.setVisible(false);
            }
            emulatorLabyrinthDialog = LabyrinthDialog.showLabyrinthDialog(parent, labyrinth, listener);
        }
        else {
            showErrorDialog("There is no generated labyrinth!", false);
        }
    }
    
    public void registerContextListener(ContextListener listener) {
        contextListeners.add(listener);
    }
    
    public void unregisterContextListener(ContextListener listener) {
        contextListeners.remove(listener);
    }
    
    public void registerRobotRunListener(RobotRunListener listener) {
        robotRunListeners.add(listener);
    }
    
    public void unregisterRobotRunListener(RobotRunListener listener) {
        robotRunListeners.remove(listener);
    }
    
    public static void showErrorDialog(String message, boolean fatal) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        if (fatal) {
            System.exit(0);
        }
    }
    
    class BasicProfileContainer extends AbstractProfileContainer {

        @Override
        public Profile getProfile() {
            return new Profile();
        }
        
    }
    
    class BorderLogUploadStrategy implements LogUploadStrategy {
        @Override
        public LogUploadStrategyDecision isUploadNeeded(
                LogUploadConfiguration arg0, LogStorageStatus status) {
            if (status.getRecordCount()>=1) {
                return LogUploadStrategyDecision.UPLOAD;
            }
            else {
                return LogUploadStrategyDecision.NOOP;
            }
        }
        
    }
    
    class BasicConfigurationStorage implements ConfigurationStorage {

        File configStorageFile = new File(CONFIG_STORAGE_FILE);
        
        @Override
        public void saveConfiguration(ByteBuffer buffer) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(configStorageFile);
                fos.write(buffer.array());
                fos.flush();
                fos.close();
            } catch (IOException e) {
               LOG.error("Failed to saveConfiguration!", e);
            }
            finally {
                if (fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {}
                }
            }
            
        }

        @Override
        public ByteBuffer loadConfiguration() {
            if (configStorageFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(configStorageFile);
                    byte[] data = IOUtils.toByteArray(fis);
                    return ByteBuffer.wrap(data);
                } catch (IOException e) {
                    LOG.error("Failed to loadConfiguration!", e);
                }
            }
            return null;
        }
        
    }
    
    class BasicSchemaStorage implements SchemaStorage {

        File configSchemaStorageFile = new File(CONFIG_SCHEMA_STORAGE_FILE);
        
        @Override
        public void saveSchema(ByteBuffer buffer) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(configSchemaStorageFile);
                fos.write(buffer.array());
                fos.flush();
                fos.close();
            } catch (IOException e) {
               LOG.error("Failed to saveSchema!", e);
            }
            finally {
                if (fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {}
                }
            }
            
        }

        @Override
        public ByteBuffer loadSchema() {
            if (configSchemaStorageFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(configSchemaStorageFile);
                    byte[] data = IOUtils.toByteArray(fis);
                    return ByteBuffer.wrap(data);
                } catch (IOException e) {
                    LOG.error("Failed to loadSchema!", e);
                }
            }
            return null;
        }
    }

    @Override
    public void onConfigurationUpdated(CommonRecord config) {
        labyrinth = labyrinthConverter.convert(labyrinth, kaa.getClient().getConfigurationManager().getConfiguration());
        fireLabirynthUpdated();
    }
    
    private void fireLabirynthUpdated() {
        for (ContextListener listener : contextListeners) {
            listener.onLabyrinthUpdated(labyrinth);
        }
        for (RobotRunListener listener : robotRunListeners) {
            listener.onLabyrinthUpdated(labyrinth);
        }
    }
    
    private void fireRobotLocationUpdated() {
        for (RobotRunListener listener : robotRunListeners) {
            listener.onRobotLocationChanged(robotPositions);
        }
    }
    
    private void fireStartRun() {
        for (ContextListener listener : contextListeners) {
            listener.onStartRun();
        }
    }
    
    private void fireFinished() {
        for (ContextListener listener : contextListeners) {
            listener.onExitFoundCompleted();
        }
        eventManager.reset();
    }
    
    private void fireEmulatorSetupComplete() {
        for (ContextListener listener : contextListeners) {
            listener.onEmulatorSetupComplete();
        }
        if (emulatorLabyrinthDialog != null && emulatorLabyrinthDialog.isVisible()) {
            emulatorLabyrinthDialog.setVisible(false);
        }
    }
    
    private void fireEmulatorInitComplete() {
        for (ContextListener listener : contextListeners) {
            listener.onEmulatorInitCompleted();
        }
    }
    
    private void fireEmulatorStopped() {
        eventManager.reset();
        for (ContextListener listener : contextListeners) {
            listener.onEmulatorStopped();
        }
        if (emulatorLabyrinthDialog != null && emulatorLabyrinthDialog.isVisible()) {
            emulatorLabyrinthDialog.setVisible(false);
        }
    }
    
    public interface RobotRunListener {
        
        void onLabyrinthUpdated(Labyrinth data);
        
        void onRobotLocationChanged(Map<String, RobotPosition> robotPositions);
        
    }

    public interface ContextListener extends RobotRunListener {
        
        void onStartRun();
        
        void onExitFoundCompleted();
        
        void onEmulatorSetupComplete();
        
        void onEmulatorInitCompleted();
        
        void onEmulatorStopped();
        
    }
    
    public interface ContextInitCallback {
        void onContextInitCompleted();
    }
    
    @Override
    public void onEventManagerInitComplete() {
        if (emulatorSetupPending) {
            emulatorSetupPending = false;
            for (Cell cell : emulatorRobots.keySet()) {
                Direction direction = emulatorRobots.get(cell);
                LOG.info("Setup emulator cell {} direction {}", cell, direction);
            }
            
            try {
                Properties robotProperties = new Properties();
                robotProperties.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT,""+ RobotEmulator.DEFAULT_COMMAND_TIMEOUT/5);
                robotProperties.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION,""+ RobotEmulator.DEFAULT_COMMAND_TIMEOUT_DEVIATION/5);
                robotProperties.put(RobotEmulator.PROPERTY_NAME_PING_TIMEOUT,""+ RobotEmulator.DEFAULT_PING_TIMEOUT/5);
                emulatorManager.setupEmulators(emulatorRobots, robotProperties);
            } catch (IOException e) {
                LOG.error("Failed to setup emulators!", e);
                showErrorDialog("Failed to setup emulators!", false);
            }
            
            emulatorManager.startEmulators();
            
            fireEmulatorSetupComplete();
        }
        else {
            callback.onContextInitCompleted();
        }
    }

    @Override
    public void onEventManagerInitFailed(String reason) {
        LOG.error("Failed to init event manager: {}", reason);
        showErrorDialog("Failed to init event manager: " + reason, true);
    }
    
    @Override
    public void onEmulatorsInitCompleted() {
        fireEmulatorInitComplete();
    }
    
    @Override
    public void onEmulatorsStopped() {
        fireEmulatorStopped();
    }

    @Override
    public void onStartRun() {
        // do nohing
    }

    @Override
    public void onExitFound(Cell cell) {
        robotPositions = new HashMap<>(eventManager.getRobotPositions());
        checkRobotPositions();
        fireRobotLocationUpdated();
    }

    @Override
    public void onExitFoundCompeted() {
        JOptionPane.showMessageDialog(null, "All robots have escaped!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
        fireFinished();
    }

    @Override
    public void onRobotLocationChanged(String key, String name, Cell cell) {
        robotPositions = new HashMap<>(eventManager.getRobotPositions());
        checkRobotPositions();
        fireRobotLocationUpdated();
    }
    
    private void checkRobotPositions() {
        Map<Cell, String> cells = new HashMap<>();
        for (String key : robotPositions.keySet()) {
            RobotPosition position = robotPositions.get(key);
            if (cells.containsKey(position.getCell())) {
                String previousKey = cells.get(position.getCell());
                RobotPosition previousPosition = robotPositions.get(previousKey);
                LOG.error("Robot collision detected at position: {}, between enpoints: [{}, {}], names [{}, {}]", 
                        position.getCell(), key, previousKey, position.getName(), previousPosition.getName());
                showErrorDialog("Robot collision detected at position: " + position, true);
            }
            else {
                cells.put(position.getCell(), key);
            }
        }
    }

}
