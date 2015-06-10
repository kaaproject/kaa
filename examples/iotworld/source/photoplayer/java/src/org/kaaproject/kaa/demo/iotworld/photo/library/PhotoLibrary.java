package org.kaaproject.kaa.demo.iotworld.photo.library;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoPlayerApplication;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoLibrary {
    
    private static final Logger LOG = LoggerFactory.getLogger(PhotoPlayerApplication.class);

    private static final int MAX_PHOTOS_IN_THUMBNAIL = 1;
    private static final String UPLOADS_ALBUM_NAME = "Uploads";
    public static final int THUMBNAIL_SIZE = 256;

    private static final String[] SUPPORTED_EXTENSIONS = { ".jpg", ".jpeg", ".png" };
    private final Path rootPath;
    private final Map<String, PhotoAlbum> albums;

    public PhotoLibrary(Path rootPath) {
        super();
        this.rootPath = rootPath;
        this.albums = new LinkedHashMap<>();
    }

    public void scan() {
        LOG.info("Scanning path: {}", rootPath.toString());

        albums.clear();
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs) throws IOException {
                    LOG.info("Processing file: {}", aFile);
                    scanFile(aFile);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path aDir, BasicFileAttributes aAttrs) throws IOException {
                    LOG.info("Processing directory: {}", aDir);
                    return FileVisitResult.CONTINUE;
                }

            });

            generateThumbnails(albums.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PhotoAlbum scanFile(Path aFile) {
        String filePath = aFile.toFile().getAbsolutePath();
        if (validate(filePath)) {
            String albumId = aFile.getParent().toFile().getAbsolutePath();
            PhotoAlbum album = albums.get(albumId);
            if (album == null) {
                album = new PhotoAlbum(albumId);
                album.getInfo().setTitle(aFile.getParent().toFile().getName());
                albums.put(albumId, album);
            }
            album.addPhoto(filePath);
            return album;
        }
        return null;
    }

    private boolean validate(String filePath) {
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (filePath.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public String getNext(String albumId) {
        if (albumId != null && albums.containsKey(albumId)) {
            List<String> keys = new ArrayList<String>(albums.keySet());
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).equals(albumId)) {
                    int pos = i + 1;
                    if (pos < keys.size()) {
                        return keys.get(pos);
                    }
                }
            }
            return keys.get(0);
        } else {
            return albums.keySet().iterator().next();
        }
    }

    public int getSize() {
        return albums.size();
    }

    public List<PhotoAlbum> getAlbums() {
        return new ArrayList<PhotoAlbum>(albums.values());
    }

    public List<PhotoAlbumInfo> buildAlbumInfoList() {
        List<PhotoAlbumInfo> albums = new ArrayList<PhotoAlbumInfo>();
        for (PhotoAlbum album : getAlbums()) {
            albums.add(album.getInfo());
        }
        return albums;
    }

    public String upload(PhotoUploadRequest event) {
        String uploadAlbumPath = getUploadsAlbumId();
        File uploadAlbumFile = new File(uploadAlbumPath);
        if (!uploadAlbumFile.exists()) {
            LOG.info("Creating upload album");
            uploadAlbumFile.mkdirs();
        }
        File photoFile = new File(uploadAlbumFile, event.getName());
        if (photoFile.exists()) {
            LOG.info("Photo with such name already exists. Will overwrite it!");
            photoFile.delete();
        }
        try (FileOutputStream os = new FileOutputStream(photoFile)) {
            os.write(toByteArray(event.getBody()));
        } catch (IOException e) {
            LOG.error("Failed to write new file", e);
            throw new RuntimeException(e);
        }
        PhotoAlbum album = scanFile(Paths.get(photoFile.toURI()));
        if(album.getPhotos().size() <= MAX_PHOTOS_IN_THUMBNAIL){
            generateThumbnail(album);
        }
        return album.getAlbumId();
    }

    public String getUploadsAlbumId() {
        return rootPath + File.separator + UPLOADS_ALBUM_NAME;
    }
    
    private void generateThumbnails(Collection<PhotoAlbum> albums) {
        for(PhotoAlbum album : albums){
            generateThumbnail(album);
        }
    }
    
    private void generateThumbnail(PhotoAlbum album) {
        if(album.getPhotos().isEmpty()){
            throw new IllegalArgumentException("Can't generate thumbnail for empty album!");
        }
        String photo = album.getPhotos().get(0);
        try {
            long time = System.currentTimeMillis();
            album.getInfo().setThumbnail(toThumbnailData(photo));
            LOG.info("Thumbnail generation time = {}", System.currentTimeMillis() - time);
        } catch (IOException e) {
            LOG.error("Faield to create thumbnail for {}", photo, e);
        }
        
    }
    
    private ByteBuffer toThumbnailData(String photo) throws IOException {
        BufferedImage image = ImageIO.read(new File(photo));
        return toThumbnailData(image);
    }

    public static ByteBuffer toThumbnailData(BufferedImage image) throws IOException {
        float size = Math.min(image.getWidth(), image.getHeight());
        
        float scale = size / THUMBNAIL_SIZE;
        
        int thumbnailWidth = (int)(image.getWidth() / scale);
        int thumbnailHeight = (int)(image.getHeight() / scale);
        BufferedImage bufferedThumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        
        Graphics2D g2 = bufferedThumbnail.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
        g2.dispose();
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        ImageIO.write(bufferedThumbnail, "jpeg", byteArrayOut);
        return ByteBuffer.wrap(byteArrayOut.toByteArray());
    }

    private static byte[] toByteArray(ByteBuffer bb) {
        byte[] data = new byte[bb.remaining()];
        bb.get(data);
        return data;
    }

    public PhotoAlbum getAlbum(String albumId) {
        return albums.get(albumId);
    }

    public void deleteUploadsAlbum() {
        String uploadDirPath = getUploadsAlbumId();
        File uploadDir = new File(uploadDirPath);
        for(File child : uploadDir.listFiles()){
            if(!child.delete()){
                LOG.warn("Failed to delete {} {}", child.getAbsolutePath(), child.isDirectory() ? "dir" : "file");
            }
        }
        if(!uploadDir.delete()){
            LOG.warn("Failed to delete {}", uploadDir.getAbsolutePath());
        }
        albums.remove(uploadDirPath);
    }
}
