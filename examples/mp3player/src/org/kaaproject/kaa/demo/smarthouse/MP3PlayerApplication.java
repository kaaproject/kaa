package org.kaaproject.kaa.demo.smarthouse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfo;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceType;
import org.kaaproject.kaa.demo.smarthouse.music.ChangeVolumeRequest;
import org.kaaproject.kaa.demo.smarthouse.music.MusicEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.music.PauseRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListResponse;
import org.kaaproject.kaa.demo.smarthouse.music.PlayRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfo;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfoResponse;
import org.kaaproject.kaa.demo.smarthouse.music.SeekRequest;
import org.kaaproject.kaa.demo.smarthouse.music.StopRequest;
import org.kaaproject.kaa.demo.smarthouse.pi.library.KaaMp3File;
import org.kaaproject.kaa.demo.smarthouse.pi.library.MediaLibrary;
import org.kaaproject.kaa.demo.smarthouse.pi.player.Mp3Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3PlayerApplication implements DeviceEventClassFamily.Listener, MusicEventClassFamily.Listener {

    private static final Logger LOG = LoggerFactory.getLogger(MP3PlayerApplication.class);
    
    private static final int UPDATE_INTERVAL = 1000;

    private static final String DEFAULT_DIR = "/media";

    private static final String DEFAULT_ACCESS_CODE = "PI_ACCESS_CODE";

    private final String accessCode;
    
    private final Path rootPath;

    private final MediaLibrary library;

    private final KaaClient client;

    private volatile Mp3Player player;
    
    private volatile StatusUpdateThread statusUpdateThread;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Path rootPath = getRootPath(args);
        String accessCode = getAccessCode(args);

        MP3PlayerApplication application = new MP3PlayerApplication(rootPath, accessCode);

        application.start();

        try {
            System.in.read();
            application.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MP3PlayerApplication(Path rootPath, String accessCode) {
        super();
        this.accessCode = accessCode;
        this.rootPath = rootPath;
        this.library = new MediaLibrary(rootPath);
        this.client = Kaa.newClient(new DesktopKaaPlatformContext());
    }

    private void start() {
        LOG.info("Going to scan folder {}", rootPath);
        library.scan();
        LOG.info("Scan folder {} complete: {} mp3 files found", rootPath, library.getSize());

        client.setEndpointAccessToken(accessCode);

        EventFamilyFactory factory = client.getEventFamilyFactory();

        factory.getDeviceEventClassFamily().addListener(this);
        factory.getMusicEventClassFamily().addListener(this);

        LOG.info("Going to start kaa client");
        client.start();
        
        statusUpdateThread = new StatusUpdateThread();
        statusUpdateThread.start();
    }

    private void stop() {
        LOG.info("Going to stop kaa client");
        client.stop();
        
        statusUpdateThread.shutdown();
    }

    @Override
    public void onEvent(DeviceInfoRequest event, String originator) {
        LOG.info("Receieved info request {}", event);
        DeviceEventClassFamily family = client.getEventFamilyFactory().getDeviceEventClassFamily();
        family.sendEvent(new DeviceInfoResponse(new DeviceInfo(DeviceType.SOUND_SYSTEM, "Raspbery pi", "UK")), originator);
    }

    @Override
    public void onEvent(PlayListRequest event, String originator) {
        LOG.info("Receieved play list request {}", event);
        MusicEventClassFamily family = client.getEventFamilyFactory().getMusicEventClassFamily();
        family.sendEvent(new PlayListResponse(library.getPlayList()), originator);
    }

    @Override
    public void onEvent(PlayRequest event, String originator) {
        LOG.info("Receieved play event {}", event);
        String songId = event.getUrl();
        player = resetPlayer(songId);
        player.play();
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(PauseRequest event, String originator) {
        LOG.info("Receieved pause event {}", event);
        player.pause();
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(StopRequest event, String originator) {
        LOG.info("Receieved stop event {}", event);
        player.stop();
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(PlaybackInfoRequest event, String originator) {
        LOG.info("Receieved playback info request {}", event);
        if (player == null) {
            return;
        }
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(ChangeVolumeRequest event, String originator) {
        LOG.info("Receieved change volume request {}", event);
        if (player == null) {
            return;
        }
        player.setVolume(event.getVolume());
        sendPlaybackResponse(originator);
    }

    @Override
    public void onEvent(SeekRequest event, String originator) {
        LOG.info("Receieved seek request {}", event);
        if (player == null) {
            return;
        }
        player.seek(event.getTime());
        sendPlaybackResponse(originator);
    }

    private synchronized Mp3Player resetPlayer(String songId) {
        KaaMp3File song = library.getSong(songId);
        if (song == null) {
            LOG.error("Can't find song {} in music library", songId);
            throw new IllegalStateException("Can't find song " + songId + " in music library");
        }
        if (player == null) {
            return new Mp3Player(song.getFilename());
        }
        if(player.getFilePath().equals(song.getFilename())){
            return player;
        }
        player.stop();
        return new Mp3Player(song.getFilename());
    }

    private void sendPlaybackResponse(String originator) {
        if(player == null){
            return;
        }
        KaaMp3File song = library.getSong(player.getFilePath());

        PlaybackInfo response = new PlaybackInfo();
        response.setSong(song.getSongInfo());
        response.setStatus(player.getStatus());
        response.setTime(player.getTime());
        response.setVolume(player.getVolume());
        response.setMaxVolume(player.getMaxVolume());
        response.setTimeSetOnDevice(true);
        response.setVolumeSetOnDevice(true);

        MusicEventClassFamily family = client.getEventFamilyFactory().getMusicEventClassFamily();
        family.sendEvent(new PlaybackInfoResponse(response), originator);
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
    
    private class StatusUpdateThread extends Thread{
        
        private volatile boolean stopped = false;
        
        @Override
        public void run(){
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
        
        
        public void shutdown(){
            stopped = true;
            this.interrupt();
        }
    }
}
