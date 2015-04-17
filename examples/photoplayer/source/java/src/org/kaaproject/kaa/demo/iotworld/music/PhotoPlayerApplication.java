package org.kaaproject.kaa.demo.iotworld.music;

import java.awt.GraphicsEnvironment;
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
import org.kaaproject.kaa.demo.iotworld.PhotoEventClassFamily;
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
import org.kaaproject.kaa.demo.iotworld.music.library.PhotoLibrary;
import org.kaaproject.kaa.demo.iotworld.music.slideshow.SlideShow;
import org.kaaproject.kaa.demo.iotworld.music.slideshow.SlideShowFrame;
import org.kaaproject.kaa.demo.iotworld.music.slideshow.SlideShowListener;
import org.kaaproject.kaa.demo.iotworld.music.storage.PhotoPlayerState;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsResponse;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest;
import org.kaaproject.kaa.demo.iotworld.photo.StopSlideShowRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoPlayerApplication implements DeviceEventClassFamily.Listener, PhotoEventClassFamily.Listener,
        GeoFencingEventClassFamily.Listener, SlideShowListener {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoPlayerApplication.class);

    private static final String DEFAULT_USER_NAME = "kaa";

    private static final String STATE_FILE_NAME = "player.state";

    private static final String DEFAULT_DEVICE_NAME = "Raspbery Pi 2 Photo Player";

    private static final String DEFAULT_DIR = "H:\\photos\\";

    private static final String DEFAULT_ACCESS_CODE = "DUMMY_ACCESS_CODE";

    private final String accessCode;

    private final Path rootPath;

    private final PhotoLibrary library;

    private final KaaClient client;

    private final DeviceEventClassFamily deviceECF;
    private final PhotoEventClassFamily photoECF;
    private final GeoFencingEventClassFamily geoECF;
    private final PhotoPlayerState state;

    private final SlideShowFrame frame;

    private volatile String deviceName = DEFAULT_DEVICE_NAME;

    private volatile SlideShow player;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Path rootPath = getRootPath(args);
        String accessCode = getAccessCode(args);

        PhotoPlayerApplication application = new PhotoPlayerApplication(rootPath, accessCode);

        application.start();

        try {
            System.in.read();
            application.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PhotoPlayerApplication(Path rootPath, String accessCode) {
        super();
        this.accessCode = accessCode;
        this.rootPath = rootPath;
        this.library = new PhotoLibrary(rootPath);
        this.client = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");

                client.attachUser(DEFAULT_USER_NAME, "", new UserAttachCallback() {

                    @Override
                    public void onAttachResult(UserAttachResponse arg0) {
                        LOG.info("Endpoint attached to default user: {}", DEFAULT_USER_NAME);
                    }
                });
            }
        });
        EventFamilyFactory factory = client.getEventFamilyFactory();
        this.deviceECF = factory.getDeviceEventClassFamily();
        this.photoECF = factory.getPhotoEventClassFamily();
        this.geoECF = factory.getGeoFencingEventClassFamily();
        this.state = new PhotoPlayerState(STATE_FILE_NAME);
        
        frame = new SlideShowFrame();
        frame.validate();
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
    }

    private void start() {
        LOG.info("Going to scan folder {}", rootPath);
        library.scan();
        LOG.info("Scan folder {} complete: {} albums found", rootPath, library.getSize());

        LOG.info("Loading music player state from file: {}", state.getFileName());
        state.load();
        LOG.info("Loaded music player state from file: {}", state.getFileName());

        state.setOperationMode(OperationMode.ON);

        handleStateUpdate();

        client.setEndpointAccessToken(accessCode);

        deviceECF.addListener(this);
        photoECF.addListener(this);
        geoECF.addListener(this);

        LOG.info("Going to start kaa client");
        client.start();
    }

    private void stop() {
        LOG.info("Going to stop kaa client");
        client.stop();
        if (player != null) {
            player.stop();
        }
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
    public void onEvent(DeviceStatusSubscriptionRequest event, String originator) {
        LOG.info("Receieved status subscription request {} from {}", event, originator);
        state.addStatusListener(originator);
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(PhotoAlbumsRequest event, String originator) {
        LOG.info("Receieved albums request {} from {}", event, originator);
        photoECF.sendEvent(new PhotoAlbumsResponse(library.buildAlbumInfoList()), originator);
    }

    @Override
    public void onEvent(PhotoUploadRequest event, String originator) {
        LOG.info("Receieved upload request {} from {}", event, originator);
        library.upload(event);
        photoECF.sendEvent(new PhotoAlbumsResponse(library.buildAlbumInfoList()), originator);
    }

    @Override
    public void onEvent(StartSlideShowRequest event, String originator) {
        LOG.info("Receieved start slideshow request {} from {}", event, originator);
        resetSlideshow(event.getAlbumId());
        player.init();
    }

    @Override
    public void onEvent(StopSlideShowRequest event, String originator) {
        LOG.info("Receieved stop slideshow request {} from {}", event, originator);
        if (player != null && player.getStatus() != SlideShowStatus.STOPPED) {
            player.stop();
        }
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
            stopPlayback();
        } else if (isPlaybackAllowed()) {
            resumePlayback();
        }
    }

    private void resumePlayback() {
        if (player == null) {
            if (state.getAlbumId() == null || library.getAlbum(state.getAlbumId()) == null) {
                state.setAlbumId(library.getNext(null));
            }
            player = resetSlideshow(state.getAlbumId());
        }
        if (player != null && player.getStatus() != SlideShowStatus.PLAYING) {
            player.init();
        }
    }

    private void stopPlayback() {
        if (player != null && player.getStatus() == SlideShowStatus.PLAYING) {
            player.stop();
        }
    }

    private SlideShow resetSlideshow(String albumId) {
        if (player != null) {
            player.stop();
        }
        player = new SlideShow(frame, library.getAlbum(albumId), this);
        return player;
    }

    private boolean isPlaybackAllowed() {
        return state.getOperationMode() == OperationMode.ON
                || (state.getOperationMode() == OperationMode.GEOFENCING && state.getGeoFencingPosition() == GeoFencingPosition.HOME);
    }

    private void sendPlaybackResponse(String originator) {
        // if (player == null) {
        // return;
        // }
        // if (originator != null) {
        // state.addStatusListener(originator);
        // }
        // KaaMp3File song = library.getSong(player.getFilePath());
        //
        // PlaybackInfo response = new PlaybackInfo();
        // response.setSong(song.getSongInfo());
        // response.setStatus(player.getStatus());
        // response.setTime(player.getTime());
        // response.setVolume(player.getVolume());
        // response.setMaxVolume(player.getMaxVolume());
        // response.setIgnoreTimeUpdate(false);
        // response.setIgnoreVolumeUpdate(false);
        //
        // for (String listener : state.getListeners()) {
        // musicECF.sendEvent(new PlaybackStatusUpdate(response), listener);
        // }
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

    @Override
    public void onSlideshowEnded() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSlideshowUpdated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInitCompleted() {
        player.play();
    }
}
