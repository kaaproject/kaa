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
package org.kaaproject.kaa.demo.iotworld.music.player;

import maryb.player.Player;
import maryb.player.PlayerEventListener;
import maryb.player.PlayerState;

import org.kaaproject.kaa.demo.iotworld.music.PlaybackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp3Player {
    private static final Logger LOG = LoggerFactory.getLogger(Mp3Player.class);

    private static final int MAX_VOLUME = 100;
    private static final int MIN_VOLUME = 50;

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
        return MAX_VOLUME - MIN_VOLUME;
    }

    public Integer getVolume() {
        return (int) ((MAX_VOLUME - MIN_VOLUME) * player.getCurrentVolume());
    }

    public synchronized void setVolume(Integer volume) {
        float newVolume = ((float) volume) / (MAX_VOLUME - MIN_VOLUME);
        int systemVolume = MIN_VOLUME + volume;

        LOG.info("New player volume: {}", newVolume);
        LOG.info("New system volume: {}", systemVolume);
        player.setCurrentVolume(newVolume);

        try {
            Process p = Runtime.getRuntime().exec("amixer cset numid=1 " + systemVolume + "%");
            p.waitFor();
        } catch (Exception e) {
            LOG.error("Failed to execute volume update", e);
        }
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
