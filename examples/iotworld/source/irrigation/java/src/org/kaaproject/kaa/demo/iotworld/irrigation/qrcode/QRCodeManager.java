package org.kaaproject.kaa.demo.iotworld.irrigation.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeManager {

    private static final Logger LOG = LoggerFactory.getLogger(QRCodeManager.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final String IMAGE_EXT = "png";
    private static final String DEFAULT_FILE_PATH = "QRCode.png";
    private static final int MAX_CACHE_MAP_SIZE = 1000;

    private final MultiFormatWriter formatWriter;
    private final Map<String, BufferedImage> imageCache;

    public QRCodeManager() {
        LOG.info("Init QRCodeManager");
        imageCache = new HashMap<>();
        formatWriter = new MultiFormatWriter();
    }

    public BufferedImage generateQRCodeImageBuffer(String authToken) {
        BufferedImage image = null;
        if (authToken != null && authToken.length() > 0) {
            image = imageCache.get(authToken);
            if (image == null) {
                Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                try {
                    image = createQRCodeBufferedImage(authToken, DEFAULT_CHARSET, hintMap, 400, 400);
                    if (imageCache.size() > MAX_CACHE_MAP_SIZE) {
                        clean();
                    }
                    imageCache.put(authToken, image);
                    LOG.info("QR Code image generated successfully!");
                } catch (WriterException | IOException e) {
                    LOG.error("Got exception during generating QRCode ", e);
                }
            }
        }
        return image;
    }

    public void writeQRCodeImageBuffer(String authToken, OutputStream out) {
        BufferedImage image = imageCache.get(authToken);
        if (image == null) {
            image = generateQRCodeImageBuffer(authToken);
        } else {
            LOG.debug("Got qrcode from cache.");
        }
        writeBufferedImageToOutputStream(image, out);
    }

    public File bufferedImageToFile(BufferedImage bufferedImage, String filePath) throws IOException {
        File outputfile = null;
        if (bufferedImage != null) {
            if (filePath != null && filePath.length() > 0) {
                outputfile = new File(filePath);
            } else {
                outputfile = new File(DEFAULT_FILE_PATH);
            }
            ImageIO.write(bufferedImage, IMAGE_EXT, outputfile);
        }
        return outputfile;
    }

    public void clean() {
        imageCache.clear();
        LOG.debug("Size of cache map exceeded limit {}", MAX_CACHE_MAP_SIZE);
    }

    private BufferedImage createQRCodeBufferedImage(String qrCodeData, Charset charset, Map<EncodeHintType, ErrorCorrectionLevel> hintMap,
            int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = formatWriter.encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth,
                qrCodeheight, hintMap);
        MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig();
        return MatrixToImageWriter.toBufferedImage(matrix, matrixToImageConfig);
    }

    private boolean writeBufferedImageToOutputStream(BufferedImage image, OutputStream out) {
        boolean result = false;
        if (out != null) {
            try {
                result = ImageIO.write(image, IMAGE_EXT, out);
            } catch (IOException e) {
                LOG.error("Can't write buffered image to stream", e);
            }
        }
        return result;
    }
}
