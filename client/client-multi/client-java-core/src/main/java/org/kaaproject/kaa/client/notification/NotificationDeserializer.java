package org.kaaproject.kaa.client.notification;

import java.io.IOException;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.schema.base.Notification;

/**
 * This class deserialize binary data to notification object.
 * 
 * This implementation is auto-generated. Please modify corresponding template file.
 * 
 * @author Andrew Shvayka
 *
 */
public class NotificationDeserializer {
    
    private final AvroByteArrayConverter<Notification> converter = new AvroByteArrayConverter<Notification>(Notification.class);
    
    Notification fromByteArray(byte[] data) throws IOException{
        return converter.fromByteArray(data);
    }

}
