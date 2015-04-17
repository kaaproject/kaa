package org.kaaproject.kaa.demo.iotworld.music.slideshow;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.kaaproject.kaa.demo.iotworld.music.PhotoPlayerApplication;
import org.kaaproject.kaa.demo.iotworld.music.library.PhotoAlbum;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlideShow implements PreviewGenerationListener {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoPlayerApplication.class);

    private static final long IMAGE_TIMEOUT = 3000;
    private static final long IMAGE_FADE = 500;

    private final SlideShowFrame frame;
    private final SlideShowListener listener;
    private final PhotoAlbum album;
    private final int width;
    private final int height;

    private volatile BufferedImage currentImage;
    private volatile BufferedImage nextImage;
    private volatile boolean initialized;
    private volatile int currentIndex;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public SlideShow(SlideShowFrame frame, PhotoAlbum album, SlideShowListener listener) {
        super();
        this.frame = frame;
        this.album = album;
        this.width = frame.getWidth();
        this.height = frame.getHeight();
        this.listener = listener;
    }

    public void stop() {
        // TODO Auto-generated method stub
    }

    public void init() {
        buildPreview(0, this);
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

                    final BufferedImage preview = new BufferedImage(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
                    Graphics2D g2 = preview.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(image, 0, 0, width, height, null);
                    g2.dispose();

                    listener.onPreviewGenerated(pos, preview);
                    LOG.info("Generated preview for photo: {}", photo);
                } catch (Exception e) {
                    listener.onPreviewGenerationFailed(e);
                }
            }
        });
    }

    public void play() {
        SlideShowThread thread = new SlideShowThread();
        thread.start();
    }

    public SlideShowStatus getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onPreviewGenerated(int index, BufferedImage preview) {
        if (index == 0 && !initialized) {
            initialized = true;
            listener.onInitCompleted();
        }
        if (currentIndex == index) {
            currentImage = preview;
        } else {
            nextImage = preview;
        }
    }

    @Override
    public void onPreviewGenerationFailed(Exception e) {
        LOG.error("Preview generation failed", e);
    }

    private class SlideShowThread extends Thread {

        private static final int ALPHA_STEP = 20;
        private static final int WAIT_FOR_PREVIEW_SLEEP_TIME = 50;
        private SlideShowPanel panel;
        private volatile boolean stopped;

        @Override
        public void run() {
            panel = new SlideShowPanel();
            panel.setBackground(frame.getBackground());
            panel.setSize(frame.getWidth(), frame.getHeight());
            frame.add(panel);
            while (!stopped) {
                try {
                    LOG.info("Scheduling preview generation for next slide: {}", getNextIndex());
                    changeAlpha((int) (panel.alpha * 100.0f), 100, ALPHA_STEP, IMAGE_FADE, currentImage, nextImage);

                    buildPreview(getNextIndex(), SlideShow.this);

                    Thread.sleep(IMAGE_TIMEOUT);

                    while (nextImage == null) {
                        LOG.info("Waiting for next preview to be generated");
                        Thread.sleep(WAIT_FOR_PREVIEW_SLEEP_TIME);
                    }

                    changeAlpha((int) (panel.alpha * 100), 0, -ALPHA_STEP, IMAGE_FADE, currentImage, nextImage);

                    currentIndex = getNextIndex();
                    currentImage = nextImage;
                    nextImage = null;
                    repaint(panel, 1.0f, currentImage, nextImage);
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
            final SlideShowPanel panel = this.panel;
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

        private volatile float alpha = 0.0f;
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
