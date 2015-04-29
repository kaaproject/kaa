package org.kaaproject.kaa.demo.iotworld.photo;

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
import org.kaaproject.kaa.demo.iotworld.photo.library.PhotoLibrary;
import org.kaaproject.kaa.demo.iotworld.photo.slideshow.SlideShow;
import org.kaaproject.kaa.demo.iotworld.photo.slideshow.SlideShowFrame;
import org.kaaproject.kaa.demo.iotworld.photo.slideshow.SlideShowListener;
import org.kaaproject.kaa.demo.iotworld.photo.storage.PhotoPlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoPlayerApplication implements DeviceEventClassFamily.Listener, PhotoEventClassFamily.Listener,
        GeoFencingEventClassFamily.Listener, SlideShowListener {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoPlayerApplication.class);

    private static final String DEFAULT_USER_NAME = "kaa";

    private static final String STATE_FILE_NAME = "player.state";

    private static final String DEFAULT_DIR = "H:\\photos\\";

    private static final String DEFAULT_ACCESS_CODE = "PHOTO_PLAYER_ACCESS_CODE";

    private final String accessCode;

    private final Path rootPath;

    private final PhotoLibrary library;

    private final KaaClient client;

    private final DeviceEventClassFamily deviceECF;
    private final PhotoEventClassFamily photoECF;
    private final GeoFencingEventClassFamily geoECF;
    private final PhotoPlayerState state;

    private final SlideShowFrame frame;

    private volatile SlideShow player;

    private volatile SlideShowStatus pendingStatus = SlideShowStatus.PAUSED;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Path rootPath = getRootPath(args);
        String accessCode = getAccessCode(args);

        PhotoPlayerApplication application = new PhotoPlayerApplication(rootPath, accessCode);

        application.start();

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                running = false;
                mainThread.interrupt();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    LOG.error("Interrupted during await termination!", e);
                }
            }
        });

        while (running) {
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                if (running) {
                    LOG.error("Interrupted during execution!", e);
                } else {
                    LOG.info("Received shutdown request!");
                }
            }
        }
        
        application.stop();
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
            player.pause();
        }
    }

    @Override
    public void onEvent(DeviceInfoRequest event, String originator) {
        LOG.info("Receieved info request {}", event);
        deviceECF.sendEvent(new DeviceInfoResponse(new DeviceInfo(state.getDeviceName(), "UK")), originator);
    }

    @Override
    public void onEvent(DeviceChangeNameRequest event, String originator) {
        state.setDeviceName(event.getName());
        deviceECF.sendEventToAll(new DeviceInfoResponse(new DeviceInfo(state.getDeviceName(), "UK")));
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
        LOG.info("Receieved upload request {} from {}", event.getName(), originator);
        try {
            String uploadedAlbumId = library.upload(event);
            player = resetSlideshow(uploadedAlbumId, true);
            player.init();
            photoECF.sendEvent(new PhotoAlbumsResponse(library.buildAlbumInfoList()), originator);
        } catch (Exception e) {
            LOG.error("Failed to process photo uplaod", e);
        }
    }

    @Override
    public void onEvent(StartSlideShowRequest event, String originator) {
        LOG.info("Receieved start slideshow request {} from {}", event, originator);
        pendingStatus = SlideShowStatus.PLAYING;
        resetSlideshow(event.getAlbumId());
        if (isPlaybackAllowed()) {
            player.init();
        }
    }

    @Override
    public void onEvent(PauseSlideShowRequest event, String originator) {
        LOG.info("Receieved stop slideshow request {} from {}", event, originator);
        pendingStatus = SlideShowStatus.PAUSED;
        if (player != null && player.getStatus() != SlideShowStatus.PAUSED) {
            player.pause();
        }
    }

    @Override
    public void onEvent(DeleteUploadedPhotosRequest event, String originator) {
        LOG.info("Receieved delete uploaded photos request {} from {}", event, originator);
        if(player != null && player.getAlbumId().equals(library.getUploadsAlbumId())){
            player.stop();
        }
        library.deleteUploadsAlbum();
        photoECF.sendEvent(new PhotoAlbumsResponse(library.buildAlbumInfoList()), originator);
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
            if (pendingStatus == SlideShowStatus.PLAYING) {
                resumePlayback();
            }
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

    private void pausePlayback() {
        if (player != null && player.getStatus() == SlideShowStatus.PLAYING) {
            player.pause();
        }
    }

    private SlideShow resetSlideshow(String albumId) {
        return resetSlideshow(albumId, false);
    }

    private SlideShow resetSlideshow(String albumId, boolean lastPhoto) {
        if (player != null) {
            player.stop();
        }
        player = new SlideShow(frame, library.getAlbum(albumId), lastPhoto, this);
        return player;
    }

    private boolean isPlaybackAllowed() {
        return state.getOperationMode() == OperationMode.ON
                || (state.getOperationMode() == OperationMode.GEOFENCING && state.getGeoFencingPosition() == GeoFencingPosition.HOME);
    }

    private void sendPlaybackResponse() {
        sendPlaybackResponse(null);
    }

    private void sendPlaybackResponse(String originator) {
        if (originator != null) {
            state.addStatusListener(originator);
        }

        PhotoFrameStatusUpdate update = new PhotoFrameStatusUpdate();
        if (player != null) {
            update.setAlbumId(player.getAlbumId());
            if (isPlaybackAllowed()) {
                update.setStatus(player.getStatus());
            } else {
                update.setStatus(pendingStatus);
            }
            update.setPhotoNumber(player.getPhotoNumber());
            update.setThumbnail(player.getThumbnail());
        }

        for (String listener : state.getListeners()) {
            photoECF.sendEvent(update, listener);
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

    @Override
    public void onSlideshowUpdated() {
        sendPlaybackResponse();
    }

    @Override
    public void onInitCompleted() {
        player.play();
    }
}
