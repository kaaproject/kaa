package org.kaaproject.kaa.demo.iotworld.music.library;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.MusicPlayerApplication;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class MediaLibrary {

    private static final String DEFAULT_ALBUM_ICON = "album.png";
    private static final int THUMBNAIL_WIDTH = 128;
    private static final int THUMBNAIL_HEIGHT = 128;

    private static final Logger LOG = LoggerFactory.getLogger(MusicPlayerApplication.class);

    private static final String MP3 = ".mp3";
    private final Path rootPath;
    private final Map<String, KaaMp3File> songs;

    public MediaLibrary(Path rootPath) {
        super();
        this.rootPath = rootPath;
        this.songs = new LinkedHashMap<>();
    }

    public void scan() {
        LOG.info("Scanning path: {}", rootPath.toString());

        songs.clear();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanFile(Path aFile) {
        String filePath = aFile.toFile().getAbsolutePath();
        if (filePath.endsWith(MP3) || filePath.endsWith(MP3.toUpperCase())) {
            try {
                KaaMp3File mp3File = new KaaMp3File(filePath);
                songs.put(filePath, mp3File);
            } catch (UnsupportedTagException | InvalidDataException | IOException e) {
                LOG.warn("Failed to scan: {}", filePath);
            }
        }
    }

    public List<KaaMp3File> getSongs() {
        return Collections.unmodifiableList(new ArrayList<>(songs.values()));
    }

    public int getSize() {
        return songs.size();
    }

    public List<AlbumInfo> getPlayList() {
        Map<String, AlbumInfo> albumMap = new HashMap<String, AlbumInfo>();

        for (KaaMp3File song : songs.values()) {
            SongInfo songInfo = song.getSongInfo();
            String albumId = songInfo.getAlbumId();
            if (!albumMap.containsKey(albumId)) {
                AlbumInfo info = new AlbumInfo();
                info.setAlbumId(albumId);
                info.setArtist(song.getArtist());
                info.setTitle(albumId);
                info.setSongs(new ArrayList<SongInfo>());
                info.setCover(getAlbumCover(albumId, song));
                albumMap.put(albumId, info);
            }
            albumMap.get(albumId).getSongs().add(songInfo);
        }

        List<AlbumInfo> albums = new ArrayList<AlbumInfo>(albumMap.values());
        Collections.sort(albums, new Comparator<AlbumInfo>() {
            @Override
            public int compare(AlbumInfo o1, AlbumInfo o2) {
                return o1.getAlbumId().compareTo(o2.getAlbumId());
            }
        });

        return albums;
    }

    private ByteBuffer getDefaultAlbumCover() {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(MediaLibrary.class.getClassLoader().getResourceAsStream(DEFAULT_ALBUM_ICON));
            return toThumbnailData(bufferedImage);
        } catch (IOException e) {
            LOG.warn("Failed to read default album cover", e);
            return null;
        }
    }

    private ByteBuffer getAlbumCover(String albumId, KaaMp3File song) {
        try {
            File file = new File(song.getFilename());
            File coverFile = new File(file.getParentFile(), "cover.jpg");
            if(coverFile.exists()){
                BufferedImage bufferedImage = ImageIO.read(coverFile);
                return toThumbnailData(bufferedImage);
            }else{
                LOG.warn("Cover file for album {} not found.", albumId);
            }
        } catch (IOException e) {
            LOG.warn("Failed to read album cover of album {}", albumId, e);
        }
        return getDefaultAlbumCover();
    }

    private ByteBuffer toThumbnailData(BufferedImage bufferedImage) throws IOException {
        Image thumbnail = bufferedImage.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
        bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);

        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        ImageIO.write(bufferedThumbnail, "png", byteArrayOut);
        return ByteBuffer.wrap(byteArrayOut.toByteArray());
    }

    public KaaMp3File getSong(String songId) {
        return songs.get(songId);
    }

    public String getNext(String songId) {
        if (songId != null && songs.containsKey(songId)) {
            List<String> keys = new ArrayList<String>(songs.keySet());
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).equals(songId)) {
                    int pos = i + 1;
                    if (pos < keys.size()) {
                        return keys.get(pos);
                    }
                }
            }
            return keys.get(0);
        } else {
            return songs.keySet().iterator().next();
        }
    }

}
