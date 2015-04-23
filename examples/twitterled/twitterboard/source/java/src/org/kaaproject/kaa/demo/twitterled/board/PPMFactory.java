package org.kaaproject.kaa.demo.twitterled.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.twitterled.board.BoardController.TwitterMessageToken;

public class PPMFactory {

    private static final float FONT_SIZE = 32.0f;
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final String FONT_FILE_NAME = "Roboto-Regular.ttf";
    private static final int MATRIX_HEIGHT = 16;
    private static final byte PPM_SEPARATOR = 0x20;
    private static final byte PPM_NEWLINE = 0x0A;

    public static void main(String[] args) throws Exception {
        List<TwitterMessageToken> tokens = new ArrayList<BoardController.TwitterMessageToken>();
        tokens.add(new TwitterMessageToken("@ashvayka", Color.RED.getRGB()));
        tokens.add(new TwitterMessageToken("Trip YoyDSoyo", Color.GREEN.getRGB()));
        tokens.add(new TwitterMessageToken("!!!", Color.BLUE.getRGB()));

        int width = createAndSave("h:\\ppm\\new.ppm", tokens, Color.BLACK.getRGB());
        System.out.println(width);
    }

    public static int createAndSave(String filePath, List<TwitterMessageToken> tokens, int background) throws Exception {
        List<BufferedImage> images = new ArrayList<BufferedImage>(tokens.size());
        int width = 0;
        for (int i = 0; i < tokens.size(); i++) {
            TwitterMessageToken token = tokens.get(i);
            String space;
            if (i == tokens.size() - 1) {
                space = "   ";
            } else {
                space = " ";
            }
            token = new TwitterMessageToken(token.getToken() + space, token.getColor());
            BufferedImage image = toScaledImage(token, background);
            images.add(image);
            width += image.getWidth();
        }
        byte[] data = toRawPPMBytes(images, width, background);
        Files.write(Paths.get(new File(filePath).toURI()), data);
        return width;
    }

    private static BufferedImage toScaledImage(TwitterMessageToken token, int background) throws Exception {
        InputStream is = PPMFactory.class.getClassLoader().getResourceAsStream(FONT_FILE_NAME);
        Font font = Font.createFont(Font.TRUETYPE_FONT, is);
        font = font.deriveFont(FONT_SIZE);

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(token.getToken());
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, (int) (height * 0.80f), BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        applyHints(g2d);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setBackground(new Color(background));
        g2d.setColor(new Color(token.getColor()));
        g2d.drawString(token.getToken(), 0, fm.getAscent() - (int) (height * 0.20f));
        g2d.dispose();

        return scale(img);
    }

    private static BufferedImage scale(BufferedImage image) {
        float scale = ((float) MATRIX_HEIGHT) / image.getHeight();
        int w = (int) (image.getWidth() * scale);
        int h = (int) (image.getHeight() * scale);

        BufferedImage resized = new BufferedImage(w, h, image.getType());
        Graphics2D g2d = resized.createGraphics();
        applyHints(g2d);
        g2d.drawImage(image, 0, 0, w, h, 0, 0, image.getWidth(), image.getHeight(), null);
        g2d.dispose();
        return resized;
    }

    private static void applyHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    public static byte[] toRawPPMBytes(List<BufferedImage> images, int width, int background) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] ppmMagicBytes = { 0x50, 0x36 };
        os.write(ppmMagicBytes);
        os.write(PPM_NEWLINE);

        os.write(Integer.toString(width).getBytes(ASCII));
        os.write(PPM_SEPARATOR);
        os.write(Integer.toString(MATRIX_HEIGHT).getBytes(ASCII));
        os.write(PPM_NEWLINE);

        os.write(Integer.toString(255).getBytes(ASCII));
        os.write(PPM_NEWLINE);

        Color color = new Color(background);

        for (int y = 0; y < MATRIX_HEIGHT; y++) {
            for (BufferedImage image : images) {
                for (int x = 0; x < image.getWidth(); x++) {
                    final int argb = image.getRGB(x, y);
                    float alpha = ((0xff & (argb >> 24)) / 255.0f);

                    int red = 0xff & (argb >> 16);
                    int green = 0xff & (argb >> 8);
                    int blue = 0xff & (argb >> 0);

                    red = (int) ((1.0f - alpha) * color.getRed() + (red * alpha));
                    green = (int) ((1.0f - alpha) * color.getGreen() + (green * alpha));
                    blue = (int) ((1.0f - alpha) * color.getBlue() + (blue * alpha));

                    os.write((byte) red);
                    os.write((byte) green);
                    os.write((byte) blue);
                }
            }
        }

        return os.toByteArray();
    }

}
