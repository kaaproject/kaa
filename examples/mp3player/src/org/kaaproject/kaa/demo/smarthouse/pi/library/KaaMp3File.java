package org.kaaproject.kaa.demo.smarthouse.pi.library;

import java.io.IOException;

import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class KaaMp3File extends Mp3File{

	private static final String UNKNOWN = "Unknown";

    public KaaMp3File(String filename) throws IOException, UnsupportedTagException, InvalidDataException {
		super(filename);
	}

    public SongInfo getSongInfo() {
        SongInfo info = new SongInfo();
        info.setTitle(getName());
        info.setDisplayName(getName());
        info.setAlbum(getAlbum());
        info.setArtist(getArtist());
        info.setDuration((int)getLengthInMilliseconds());
        info.setUrl(getFilename());
        return info;
    }

    private String getArtist() {
        if(hasId3v1Tag()){
            return notNull(getId3v1Tag().getArtist());
        } else if (hasId3v2Tag()){
            return notNull(getId3v2Tag().getArtist());
        }
        return UNKNOWN;
    }

    private String getAlbum() {
        if(hasId3v1Tag()){
            return notNull(getId3v1Tag().getAlbum());
        } else if (hasId3v2Tag()){
            return notNull(getId3v2Tag().getAlbum());
        }
        return UNKNOWN;
    }

    private String getName() {
        if(hasId3v1Tag()){
            return notNull(getId3v1Tag().getTitle());
        } else if (hasId3v2Tag()){
            return notNull(getId3v2Tag().getTitle());
        }
        return UNKNOWN;
    }
    
    private static final String notNull(String source){
        if(source != null){
            return source;
        }else{
            return UNKNOWN;
        }
    }
	
}
