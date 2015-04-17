package org.kaaproject.kaa.demo.iotworld.music.slideshow;

import java.awt.image.BufferedImage;

public interface PreviewGenerationListener {

    public void onPreviewGenerated(int index, BufferedImage preview);
    
    public void onPreviewGenerationFailed(Exception e);
}
