package org.kaaproject.kaa.demo.iotworld.music.player;

import java.util.LinkedList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import maryb.player.Player;
import maryb.player.PlayerEventListener;
import maryb.player.PlayerState;

import org.kaaproject.kaa.demo.iotworld.music.PlaybackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp3Player {
    private static final Logger LOG = LoggerFactory.getLogger(Mp3Player.class);

    private static final float MAX_VOLUME = 100.0f;

    private static final float DEFAULT_VOLUME = 1.0f;

    private final String filePath;
    private final Player player;

    public Mp3Player(String filePath) {
        super();
        this.filePath = filePath;
        this.player = new Player();
        player.setSourceLocation(filePath);
        player.setCurrentVolume(DEFAULT_VOLUME);
    }

    public void clearEndOfMediaListener() {
        player.setListener(null);
    }

    public void setEndOfMediaListener(final EndOfMediaListener listener) {
        player.setListener(new PlayerEventListener() {

            @Override
            public void stateChanged() {
                // TODO Auto-generated method stub

            }

            @Override
            public void endOfMedia() {
                listener.onEndOfMedia();
            }

            @Override
            public void buffer() {
                // TODO Auto-generated method stub

            }
        });
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
        LinkedList<Line> speakers = new LinkedList<Line>();

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
//            if (!mixerInfo.getName().equals("Java Sound Audio Engine"))
//                continue;

            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] lines = mixer.getTargetLineInfo();

            for (Line.Info info : lines) {

                try {
                    Line line = mixer.getLine(info);
                    speakers.add(line);
                } catch (Exception e) {
                    LOG.trace("Failed to get line", e);
                }
            }
        }

        for (Line line : speakers) {
            try {
                boolean opened = line.isOpen() || line instanceof Clip;
                if (!opened)
                {
                    line.open();
                }
                FloatControl control = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
                control.setValue(control.getMaximum() * newVolume);
            } catch (Exception e) {
                LOG.trace("Failed to change line volume", e);
            }
        }

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
