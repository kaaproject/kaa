package org.kaaproject.kaa.demo.iotworld.photo.slideshow;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public interface PreviewGenerationListener {

    public void onPreviewGenerated(int index, BufferedImage preview, ByteBuffer thumbnail);
    
    public void onPreviewGenerationFailed(Exception e);
}
