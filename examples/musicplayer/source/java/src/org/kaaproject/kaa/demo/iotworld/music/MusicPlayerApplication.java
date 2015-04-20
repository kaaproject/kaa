package org.kaaproject.kaa.demo.iotworld.music;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.iotworld.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.GeoFencingEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.MusicEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfo;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPosition;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse;
import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.music.library.KaaMp3File;
import org.kaaproject.kaa.demo.iotworld.music.library.MediaLibrary;
import org.kaaproject.kaa.demo.iotworld.music.player.EndOfMediaListener;
import org.kaaproject.kaa.demo.iotworld.music.player.Mp3Player;
import org.kaaproject.kaa.demo.iotworld.music.storage.MusicPlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicPlayerApplication implements DeviceEventClassFamily.Listener, MusicEventClassFamily.Listener,
        GeoFencingEventClassFamily.Listener, EndOfMediaListener {

    private static final Logger LOG = LoggerFactory.getLogger(MusicPlayerApplication.class);

    private static final String DEFAULT_USER_NAME = "kaa";

    private static final String STATE_FILE_NAME = "player.state";

    private static final String DEFAULT_DEVICE_NAME = "Raspbery Pi 2 Music Player";

    private static final int UPDATE_INTERVAL = 1000;

    private static final String DEFAULT_DIR = "E:\\music\\";

    private static final String DEFAULT_ACCESS_CODE = "DUMMY_ACCESS_CODE";

    private final String accessCode;

    private final Path rootPath;

    private final MediaLibrary library;

    private final KaaClient client;

    private final DeviceEventClassFamily deviceECF;
    private final MusicEventClassFamily musicECF;
    private final GeoFencingEventClassFamily geoECF;
    private final MusicPlayerState state;

    private volatile Mp3Player player;
    private volatile StatusUpdateThread statusUpdateThread;
    private volatile String deviceName = DEFAULT_DEVICE_NAME;

    private volatile PlaybackStatus pendingStatus = PlaybackStatus.STOPPED;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Path rootPath = getRootPath(args);
        String accessCode = getAccessCode(args);

        MusicPlayerApplication application = new MusicPlayerApplication(rootPath, accessCode);

        application.start();

        try {
            System.in.read();
            application.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MusicPlayerApplication(Path rootPath, String accessCode) {
        super();
        this.accessCode = accessCode;
        this.rootPath = rootPath;
        this.library = new MediaLibrary(rootPath);
        this.client = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                client.attachUser(DEFAULT_USER_NAME, "", new UserAttachCallback() {

                    @Override
                    public void onAttachResult(UserAttachResponse arg0) {
                        LOG.info("Endpoint attached to default user: {}", DEFAULT_USER_NAME);
                    }
                });
                LOG.info("Kaa client started");
                statusUpdateThread = new StatusUpdateThread();
                statusUpdateThread.start();
            }
        });
        EventFamilyFactory factory = client.getEventFamilyFactory();
        this.deviceECF = factory.getDeviceEventClassFamily();
        this.musicECF = factory.getMusicEventClassFamily();
        this.geoECF = factory.getGeoFencingEventClassFamily();
        this.state = new MusicPlayerState(STATE_FILE_NAME);
    }

    private void start() {
        LOG.info("Going to scan folder {}", rootPath);
        library.scan();
        LOG.info("Scan folder {} complete: {} mp3 files found", rootPath, library.getSize());

        LOG.info("Loading music player state from file: {}", state.getFileName());
        state.load();
        LOG.info("Loaded music player state from file: {}", state.getFileName());

        client.setEndpointAccessToken(accessCode);

        deviceECF.addListener(this);
        musicECF.addListener(this);
        geoECF.addListener(this);

        LOG.info("Going to start kaa client");
        client.start();
    }

    private void stop() {
        LOG.info("Going to stop kaa client");
        client.stop();
        statusUpdateThread.shutdown();
    }

    @Override
    public void onEvent(DeviceInfoRequest event, String originator) {
        LOG.info("Receieved info request {}", event);
        deviceECF.sendEvent(new DeviceInfoResponse(new DeviceInfo(deviceName, "UK")), originator);
    }

    @Override
    public void onEvent(DeviceChangeNameRequest event, String originator) {
        deviceName = event.getName();
        deviceECF.sendEventToAll(new DeviceInfoResponse(new DeviceInfo(deviceName, "UK")));
    }

    @Override
    public void onEvent(PlayListRequest event, String originator) {
        LOG.info("Receieved play list request {} from {}", event, originator);
        musicECF.sendEvent(new PlayListResponse(library.getPlayList()), originator);
    }

    @Override
    public void onEvent(PlayRequest event, String originator) {
        LOG.info("Receieved play event {} from {}", event, originator);
        String songId = event.getUrl();
        if (player == null || !(player.getStatus() == PlaybackStatus.PAUSED && player.getFilePath().equals(songId))) {
            player = resetPlayer(songId);
        }
        pendingStatus = PlaybackStatus.PLAYING;
        if (isPlaybackAllowed()) {
            player.play();
        }
        state.setSongId(songId);
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(PauseRequest event, String originator) {
        LOG.info("Receieved pause event {} from {}", event, originator);
        pendingStatus = PlaybackStatus.PAUSED;
        player.pause();
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(StopRequest event, String originator) {
        LOG.info("Receieved stop event {} from {}", event, originator);
        pendingStatus = PlaybackStatus.STOPPED;
        player.stop();
        state.setSongId(null);
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(DeviceStatusSubscriptionRequest event, String originator) {
        LOG.info("Receieved playback info request {} from {}", event, originator);
        state.addStatusListener(originator);
        if (player == null) {
            return;
        }
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(ChangeVolumeRequest event, String originator) {
        LOG.info("Receieved change volume request {} from {}", event, originator);
        if (player == null) {
            return;
        }
        player.setVolume(event.getVolume());
        sendPlaybackResponse(originator, false, true);
    }

    @Override
    public void onEvent(SeekRequest event, String originator) {
        LOG.info("Receieved seek request {}", event);
        if (player == null) {
            return;
        }
        player.seek(event.getTime());
        sendPlaybackResponse(originator, true, false);
    }

    @Override
    public void onEvent(GeoFencingStatusRequest event, String originator) {
        LOG.info("Receieved geo fencing status request {} from {}", event, originator);
        GeoFencingStatusResponse response = new GeoFencingStatusResponse(state.getOperationMode(), state.getGeoFencingPosition());
        geoECF.sendEvent(response, originator);
    }

    @Override
    public void onEvent(OperationModeUpdateRequest event, String originator) {
        LOG.info("Receieved operation mode update {} from {}", event, originator);
        state.setOperationMode(event.getMode());

        handleStateUpdate();

        GeoFencingStatusResponse response = new GeoFencingStatusResponse(state.getOperationMode(), state.getGeoFencingPosition());
        for (String listener : state.getListeners()) {
            geoECF.sendEvent(response, listener);
        }
    }

    @Override
    public void onEvent(GeoFencingPositionUpdate event, String originator) {
        LOG.info("Receieved geo fencing update {} from {}", event, originator);
        state.setGeoFencingPosition(event.getPosition());

        handleStateUpdate();

        GeoFencingStatusResponse response = new GeoFencingStatusResponse(state.getOperationMode(), state.getGeoFencingPosition());
        for (String listener : state.getListeners()) {
            geoECF.sendEvent(response, listener);
        }
    }

    private void handleStateUpdate() {
        LOG.info("Processing state update. Operation mode {} and geo fencing position {}", state.getOperationMode(),
                state.getGeoFencingPosition());
        if (state.getOperationMode() == OperationMode.OFF) {
            pausePlayback();
        } else if (isPlaybackAllowed()) {
            if(pendingStatus == PlaybackStatus.PLAYING){
                resumePlayback();
            }
        }
    }

    private void resumePlayback() {
        if (player == null) {
            if (state.getSongId() == null || library.getSong(state.getSongId()) == null) {
                state.setSongId(library.getNext(null));
            }
            player = resetPlayer(state.getSongId());
        }
        if (player != null && player.getStatus() != PlaybackStatus.PLAYING) {
            player.play();
        }
    }

    private void pausePlayback() {
        if (player != null && player.getStatus() == PlaybackStatus.PLAYING) {
            player.pause();
        }
    }

    @Override
    public void onEndOfMedia() {
        LOG.info("Playback of song {} completed", state.getSongId());
        if (isPlaybackAllowed()) {
            String nextSongId = library.getNext(state.getSongId());
            LOG.info("Next song {}", nextSongId);
            resetPlayer(nextSongId);
            player.play();
            state.setSongId(nextSongId);
        }
    }

    private boolean isPlaybackAllowed() {
        return state.getOperationMode() == OperationMode.ON
                || (state.getOperationMode() == OperationMode.GEOFENCING && state.getGeoFencingPosition() == GeoFencingPosition.HOME);
    }

    private synchronized Mp3Player resetPlayer(String songId) {
        KaaMp3File song = library.getSong(songId);
        if (song == null) {
            LOG.error("Can't find song {} in music library", songId);
            throw new IllegalStateException("Can't find song " + songId + " in music library");
        }
        if (player != null) {
            player.clearEndOfMediaListener();
            player.stop();
        }

        player = new Mp3Player(song.getFilename());
        player.setEndOfMediaListener(this);
        return player;
    }

    private void sendPlaybackResponse(String originator) {
        sendPlaybackResponse(originator, false, false);
    }

    private void sendPlaybackResponse(String originator, boolean ignoreOriginatorTimeUpdate, boolean ignoreOriginatorVolumeUpdate) {
        if (player == null) {
            return;
        }
        if (originator != null) {
            state.addStatusListener(originator);
        }
        KaaMp3File song = library.getSong(player.getFilePath());

        PlaybackInfo response = new PlaybackInfo();
        response.setSong(song.getSongInfo());
        if(isPlaybackAllowed()){;
            response.setStatus(player.getStatus());
        }else{
            response.setStatus(pendingStatus);
        }
        response.setTime(player.getTime());
        response.setVolume(player.getVolume());
        response.setMaxVolume(player.getMaxVolume());

        for (String listener : state.getListeners()) {
            if (listener.equals(originator)) {
                response.setIgnoreTimeUpdate(ignoreOriginatorTimeUpdate);
                response.setIgnoreVolumeUpdate(ignoreOriginatorVolumeUpdate);
            } else {
                response.setIgnoreTimeUpdate(false);
                response.setIgnoreVolumeUpdate(false);
            }
            musicECF.sendEvent(new PlaybackStatusUpdate(response), listener);
        }
    }

    private static Path getRootPath(String[] args) {
        Path rootPath = Paths.get(getRootDir(args));
        File rootDir = rootPath.toFile();
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("Path " + rootPath.toString() + " is not a valid directory");
        }
        return rootPath;
    }

    private static String getAccessCode(String[] args) {
        String accessCode;
        if (args.length >= 2) {
            accessCode = args[1];
        } else {
            accessCode = DEFAULT_ACCESS_CODE;
        }
        return accessCode;
    }

    private static String getRootDir(String[] args) {
        String rootPath;
        if (args.length >= 1) {
            rootPath = args[0];
        } else {
            rootPath = DEFAULT_DIR;
        }
        return rootPath;
    }

    private class StatusUpdateThread extends Thread {

        private volatile boolean stopped = false;

        @Override
        public void run() {
            while (!stopped) {
                sendPlaybackResponse(null);
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    stopped = true;
                    break;
                }
            }
        }

        public void shutdown() {
            stopped = true;
            this.interrupt();
        }
    }
}
