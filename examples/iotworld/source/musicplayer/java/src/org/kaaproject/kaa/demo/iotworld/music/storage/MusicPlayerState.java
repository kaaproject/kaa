package org.kaaproject.kaa.demo.iotworld.music.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPosition;
import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.music.MusicPlayerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicPlayerState {

    private static final String SONG_ID_PROP_NAME = "song.id";
    private static final String GEO_FENCING_PROP_NAME = "geofencing.position";
    private static final String OPERATION_MODE_PROP_NAME = "operation.mode";
    private static final String LISTENERS_PROP_NAME = "listeners";
    private static final String LISTENERS_PROP_DEFAULT = "";
    private static final String LISTENERS_PROP_DELIMITER = ",";

    private static final Logger LOG = LoggerFactory.getLogger(MusicPlayerApplication.class);

    private final String fileName;
    private final Properties properties;
    private final Set<String> listeners = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public MusicPlayerState(String fileName) {
        this.fileName = fileName;
        this.properties = new Properties();
    }

    public synchronized void load() {
        if(new File(fileName).exists()){
            try (InputStream is = new FileInputStream(fileName)) {
                properties.load(is);
                LOG.info("Loaded state file.");
            } catch (IOException e) {
                LOG.error("Failed to load state file. Fallback to default values", e);
            }
        }else{
            LOG.info("State file does not exist. Probably due to first start!");
        }
        String listenersStr = properties.getProperty(LISTENERS_PROP_NAME, LISTENERS_PROP_DEFAULT);
        for(String listener : listenersStr.split(LISTENERS_PROP_DELIMITER)){
            if(!listener.isEmpty()){
                listeners.add(listener);
            }
        }
    }
    
    private synchronized void persist() {
        StringBuilder sb = new StringBuilder();
        for(String listener : listeners){
            sb.append(listener).append(LISTENERS_PROP_DELIMITER);
        }
        properties.put(LISTENERS_PROP_NAME, sb.toString());
        try (OutputStream os = new FileOutputStream(fileName)) {
            properties.store(os, null);
        } catch (IOException e) {
            LOG.error("Failed to persist state file. Fallback to default values", e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public OperationMode getOperationMode() {
        String opModeName = properties.getProperty(OPERATION_MODE_PROP_NAME, OperationMode.OFF.name());
        return OperationMode.valueOf(opModeName);
    }

    public GeoFencingPosition getGeoFencingPosition() {
        String geoFencingPosName = properties.getProperty(GEO_FENCING_PROP_NAME, GeoFencingPosition.AWAY.name());
        return GeoFencingPosition.valueOf(geoFencingPosName);
    }

    public void setOperationMode(OperationMode mode) {
        if(mode == null){
            LOG.warn("Operation mode is null");
            return;
        }
        properties.setProperty(OPERATION_MODE_PROP_NAME, mode.name());
        persist();
    }

    public void setGeoFencingPosition(GeoFencingPosition position) {
        if(position == null){
            LOG.warn("Geofencing position is null");
            return;
        }
        properties.setProperty(GEO_FENCING_PROP_NAME, position.name());
        persist();
    }

    public void addStatusListener(String originator) {
        if(listeners.add(originator)){
            persist();
        }
    }

    public Set<String> getListeners() {
        return listeners;
    }

    public String getSongId() {
        return properties.getProperty(SONG_ID_PROP_NAME);
    }

    public void setSongId(String songId) {
        properties.setProperty(SONG_ID_PROP_NAME, songId);
        persist();
    }

}
