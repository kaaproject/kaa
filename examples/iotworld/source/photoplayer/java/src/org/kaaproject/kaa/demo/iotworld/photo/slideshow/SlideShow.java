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
package org.kaaproject.kaa.demo.iotworld.photo.slideshow;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoPlayerApplication;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.photo.library.PhotoAlbum;
import org.kaaproject.kaa.demo.iotworld.photo.library.PhotoLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlideShow implements PreviewGenerationListener {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoPlayerApplication.class);

    private static final long IMAGE_TIMEOUT = 5000;
    private static final long IMAGE_FADE = 500;

    private final SlideShowFrame frame;
    private final SlideShowListener listener;
    private final PhotoAlbum album;
    private final SlideShowThread thread;

    private volatile BufferedImage currentImage;   
    private volatile ByteBuffer currentImageThumbnail;
    private volatile BufferedImage nextImage;
    private volatile ByteBuffer nextImageThumbnail;
    private volatile boolean initialized;
    private volatile int currentIndex;
    private volatile SlideShowStatus status;
    
    private volatile SlideShowPanel panel;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public SlideShow(SlideShowFrame frame, PhotoAlbum album, boolean lastOne, SlideShowListener listener) {
        super();
        this.frame = frame;
        this.album = album;
        this.listener = listener;
        this.thread = new SlideShowThread();
        if(lastOne){
            currentIndex = album.getPhotos().size() - 1;
        }
    }
    
    public void play() {
        status = SlideShowStatus.PLAYING;
        thread.play();
    }

    public void pause() {
        status = SlideShowStatus.PAUSED;
        thread.pause();
    }
    
    public void stop() {
        if(panel != null){
            frame.remove(panel);
        }
        thread.stopped = true;
        thread.interrupt();
    }

    public void init() {
        buildPreview(currentIndex, this);
    }

    private void buildPreview(final int pos, final PreviewGenerationListener listener) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String photo = album.getPhotos().get(pos);
                    LOG.info("Generating preview for photo: {}", photo);
                    File photoFile = new File(photo);

                    BufferedImage image = ImageIO.read(photoFile);

                    final BufferedImage preview = scale(image, frame.getWidth(), frame.getHeight());

                    listener.onPreviewGenerated(pos, preview, PhotoLibrary.toThumbnailData(image));
                    LOG.info("Generated preview for photo: {}", photo);
                } catch (Exception e) {
                    listener.onPreviewGenerationFailed(e);
                }
            }

            private BufferedImage scale(BufferedImage image, int width, int height) {
                final BufferedImage preview = new BufferedImage(width, height, Image.SCALE_SMOOTH);
                Graphics2D g2 = preview.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(image, 0, 0, width, height, null);
                g2.dispose();
                return preview;
            }
        });
    }

    public String getAlbumId() {
        return album.getAlbumId();
    }
    
    public SlideShowStatus getStatus() {
        return status;
    }

    public Integer getPhotoNumber() {
        return currentIndex + 1;
    }

    public ByteBuffer getThumbnail() {
        return currentImageThumbnail;
    }

    @Override
    public void onPreviewGenerated(int index, BufferedImage preview, ByteBuffer thumbnail) {
        if (currentIndex == index) {
            currentImage = preview;
            currentImageThumbnail = thumbnail;
        } else {
            nextImage = preview;
            nextImageThumbnail = thumbnail;
        }
        if (index == currentIndex && !initialized) {
            initialized = true;
            listener.onInitCompleted();
        }
    }

    @Override
    public void onPreviewGenerationFailed(Exception e) {
        LOG.error("Preview generation failed", e);
    }

    private class SlideShowThread extends Thread {

        private static final int ALPHA_STEP = 20;
        private static final int WAIT_FOR_PREVIEW_SLEEP_TIME = 50;
        private volatile boolean started;
        private volatile boolean paused;
        private volatile boolean stopped;

        public void play() {
            paused = false;
            if(!started){
                started = true;
                start();
            }
        }

        public void pause() {
            paused = true;
            listener.onSlideshowUpdated();
        }

        @Override
        public void run() {
            panel = new SlideShowPanel();
            panel.setBackground(frame.getBackground());
            panel.setSize(frame.getWidth(), frame.getHeight());
            frame.add(panel);
            listener.onSlideshowUpdated();
            while (!stopped) {
                try {
                    if(paused){
                        Thread.sleep(100);
                        continue;
                    }
                    LOG.info("Scheduling preview generation for next slide: {}", getNextIndex());
//                    RASPBERRY PI is not powerful enough to show this effect smoothly
//                    changeAlpha((int) (panel.alpha * 100.0f), 100, ALPHA_STEP, IMAGE_FADE, currentImage, nextImage);

                    repaint(panel, 1.0f, currentImage, nextImage);
                    listener.onSlideshowUpdated();
                    
                    buildPreview(getNextIndex(), SlideShow.this);

                    Thread.sleep(IMAGE_TIMEOUT);

                    while (nextImage == null) {
                        LOG.info("Waiting for next preview to be generated");
                        Thread.sleep(WAIT_FOR_PREVIEW_SLEEP_TIME);
                    }

//                    RASPBERRY PI is not powerful enough to show this effect smoothly
//                    changeAlpha((int) (panel.alpha * 100), 0, -ALPHA_STEP, IMAGE_FADE, currentImage, nextImage);

                    currentIndex = getNextIndex();
                    currentImage = nextImage;
                    currentImageThumbnail = nextImageThumbnail;
                    nextImage = null;
//                    repaint(panel, 1.0f, currentImage, nextImage);
                } catch (InterruptedException e) {
                    LOG.warn("Slideshow thread interrupted", e);
                }
            }
        }

        private int getNextIndex() {
            if (currentIndex == album.getPhotos().size() - 1) {
                return 0;
            } else {
                return currentIndex + 1;
            }
        }

        private void changeAlpha(int from, int to, int step, long duration, final BufferedImage current, final BufferedImage next) throws InterruptedException {
            final SlideShowPanel panel = SlideShow.this.panel;
            if (to == from) {
                return;
            }
            long tick = duration / ((to - from) / step);
            for (int alpha = from; alpha != to; alpha += step) {
                long time = System.currentTimeMillis();
                setAlpha(panel, alpha, current, next);
                long spent = System.currentTimeMillis() - time;
                time = tick - spent;
                if (time > 0) {
                    LOG.info("Going to sleep for {} ms", time);
                    Thread.sleep(time);
                }
            }
            setAlpha(panel, to, current, next);
        }

        private void setAlpha(final SlideShowPanel panel, int alpha, final BufferedImage current, final BufferedImage next) throws InterruptedException {
            LOG.info("Setting panel alpha to {}, {}, {}", alpha, current != null ? current.hashCode() : "null", next != null ? next.hashCode() : "null");
            repaint(panel, alpha / 100.0f, current, next);
        }

        private void repaint(final SlideShowPanel panel, final float alpha, final BufferedImage current, final BufferedImage next) {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        panel.setCurrent(current);
                        panel.setNext(next);
                        panel.setAlpha(alpha);
                        panel.validate();
                        panel.repaint();
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                LOG.error("Failed to repaint!");
            }
        }
    }

    private class SlideShowPanel extends JPanel {

        private static final long serialVersionUID = -6643896730166128279L;

        private volatile float alpha = 1.0f;
        private volatile BufferedImage current;
        private volatile BufferedImage next;

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }
        
        public void setCurrent(BufferedImage current){
            this.current = current;
        }
        
        public void setNext(BufferedImage next){
            this.next = next;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            float alphaCopy = alpha;
            BufferedImage currentCopy = current;
            BufferedImage nextCopy = next;
            
            if(currentCopy == null){
                return;
            }
            
            LOG.info("PAINT: {}, {}, {}, {}", alphaCopy, currentIndex, currentCopy.hashCode(), nextCopy != null ? nextCopy.hashCode() : "null");
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setBackground(Color.BLACK);
            g2d.setComposite(AlphaComposite.SrcOver.derive(alphaCopy));
            g2d.drawImage(currentCopy, 0, 0, this);

            if (alphaCopy != 1.0f && nextCopy != null) {
                g2d.setComposite(AlphaComposite.SrcOver.derive(1f - alphaCopy));
                g2d.drawImage(nextCopy, 0, 0, this);
            }

            g2d.dispose();
        }
    }
}
