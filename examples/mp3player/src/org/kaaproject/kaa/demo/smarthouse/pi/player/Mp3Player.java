package org.kaaproject.kaa.demo.smarthouse.pi.player;

import maryb.player.Player;
import maryb.player.PlayerState;

import org.kaaproject.kaa.demo.smarthouse.music.PlaybackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp3Player {
    private static final Logger LOG = LoggerFactory.getLogger(Mp3Player.class);

    private static final float MAX_VOLUME = 100.0f;

    private static final float DEFAULT_VOLUME = 0.25f;

    private final String filePath;
    private final Player player;

    public Mp3Player(String filePath) {
        super();
        this.filePath = filePath;
        this.player = new Player();
        player.setSourceLocation(filePath);
        player.setCurrentVolume(DEFAULT_VOLUME);
    }

    public String getFilePath() {
        return filePath;
    }

    public void play() {
        if (player.getState() != PlayerState.PLAYING) {
            try {
                player.playSync();
            } catch (InterruptedException e) {
                LOG.error("Failed to modify player state", e);
            }
        }
    }

    public void stop() {
        if (player.getState() != PlayerState.STOPPED) {
            try {
                player.stopSync();
            } catch (InterruptedException e) {
                LOG.error("Failed to modify player state", e);
            }
        }
    }

    public void pause() {
        if (player.getState() != PlayerState.PAUSED) {
            try {
                player.pauseSync();
            } catch (InterruptedException e) {
                LOG.error("Failed to modify player state", e);
            }
        }
    }
    
    public void seek(Integer time) {
        player.seek(time * 1000);
    }

    public Integer getTime() {
        return (int) (player.getCurrentPosition() / 1000);
    }

    public Integer getMaxVolume() {
        return (int) MAX_VOLUME;
    }

    public Integer getVolume() {
        return (int) (MAX_VOLUME * player.getCurrentVolume());
    }

    public void setVolume(Integer volume) {
        float newVolume = volume / MAX_VOLUME;
        player.setCurrentVolume(newVolume);
    }

    public PlaybackStatus getStatus() {
        switch (player.getState()) {
        case PLAYING:
            return PlaybackStatus.PLAYING;
        case STOPPED:
            return PlaybackStatus.STOPPED;
        case PAUSED:
            return PlaybackStatus.PAUSED;
        case PAUSED_BUFFERING:
            return PlaybackStatus.PLAYING;
        }
        return PlaybackStatus.STOPPED;
    }
}
