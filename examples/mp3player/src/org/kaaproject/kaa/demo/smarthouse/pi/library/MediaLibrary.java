package org.kaaproject.kaa.demo.smarthouse.pi.library;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.demo.smarthouse.MP3PlayerApplication;
import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class MediaLibrary {

	private static final Logger LOG = LoggerFactory
			.getLogger(MP3PlayerApplication.class);

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
				public FileVisitResult visitFile(Path aFile,
						BasicFileAttributes aAttrs) throws IOException {
					LOG.info("Processing file: {}", aFile);
					scanFile(aFile);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path aDir,
						BasicFileAttributes aAttrs) throws IOException {
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
				// TODO: add ability to get SongInfo from KaaMp3File;
				songs.put(filePath, mp3File);
			} catch (UnsupportedTagException | InvalidDataException
					| IOException e) {
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

    public List<SongInfo> getPlayList() {
        List<SongInfo> playList = new ArrayList<>(songs.size());
        for(KaaMp3File song : songs.values()){
            playList.add(song.getSongInfo());
        }
        
        return playList;
    }

    public KaaMp3File getSong(String songId) {
        return songs.get(songId);
    }

}
