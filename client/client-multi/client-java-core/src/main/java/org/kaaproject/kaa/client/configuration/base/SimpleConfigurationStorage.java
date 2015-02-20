package org.kaaproject.kaa.client.configuration.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConfigurationStorage implements ConfigurationStorage {
    private static final int _8KB = 1024 * 8;
    private static final Logger LOG = LoggerFactory.getLogger(SimpleConfigurationStorage.class);

    private final KaaClientPlatformContext context;
    private final String path;

    public SimpleConfigurationStorage(KaaClientPlatformContext context, String path) {
        this.context = context;
        this.path = path;
    }

    @Override
    public void saveConfiguration(ByteBuffer buffer) throws IOException {
        PersistentStorage storage = context.createPersistentStorage();
        BufferedOutputStream os = new BufferedOutputStream(storage.openForWrite(path));
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        LOG.trace("Writing {} bytes to output stream", data.length);
        os.write(data);
        os.close();
    }

    @Override
    public ByteBuffer loadConfiguration() throws IOException {
        PersistentStorage storage = context.createPersistentStorage();
        BufferedInputStream is = new BufferedInputStream(storage.openForRead(path));
        List<byte[]> chunks = new ArrayList<byte[]>();
        byte[] tmp = new byte[_8KB];
        int size = 0;
        while(true){
            int result = is.read(tmp);
            LOG.trace("Reading {} bytes from input stream", result);
            size += result;
            if(result > 0){
                chunks.add(Arrays.copyOf(tmp, result));
            }
            if(result < tmp.length){
                break;
            }
        }
        ByteBuffer data = ByteBuffer.wrap(new byte[size]);
        for(byte[] chunk : chunks){
            data.put(chunk);
        }
        is.close();
        return data;
    }
}
