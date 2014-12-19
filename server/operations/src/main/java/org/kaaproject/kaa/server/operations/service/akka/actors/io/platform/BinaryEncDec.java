package org.kaaproject.kaa.server.operations.service.akka.actors.io.platform;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointVersionInfo;
import org.kaaproject.kaa.server.operations.pojo.sync.EventClassFamilyVersionInfo;
import org.kaaproject.kaa.server.operations.pojo.sync.LogClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.LogEntry;
import org.kaaproject.kaa.server.operations.pojo.sync.ProfileClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.UserAttachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.UserClientSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryEncDec implements PlatformEncDec {

    // General constants
    private static final Logger LOG = LoggerFactory.getLogger(BinaryEncDec.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int PADDING_SIZE = 4;
    private static final int MIN_SIZE_OF_MESSAGE_HEADER = 8;
    private static final int MIN_SIZE_OF_EXTENSION_HEADER = 8;
    private static final int PROTOCOL_ID = 0; // TODO: update value of constant
    private static final int MIN_SUPPORTED_VERSION = 1;
    private static final int MAX_SUPPORTED_VERSION = 1;

    // Extension constants
    private static final int META_DATA_EXTENSION_ID = 1;
    private static final int PROFILE_EXTENSION_ID = 2;
    private static final int USER_EXTENSION_ID = 3;
    private static final int LOGGING_EXTENSION_ID = 4;
    private static final int CONFIGURATION_EXTENSION_ID = 5;
    private static final int NOTIFICATION_EXTENSION_ID = 6;
    private static final int EVENT_EXTENSION_ID = 7;

    // Meta data constants
    private static final int PUBLIC_KEY_HASH_SIZE = 128;
    private static final int PUBLIC_KEY_SIZE = 128;

    // Profile client sync fields
    private static final int CONF_SCHEMA_VERSION_FIELD_ID = 0;
    private static final int PROFILE_SCHEMA_VERSION_FIELD_ID = 1;
    private static final int SYSTEM_NOTIFICATION_SCHEMA_VERSION_FIELD_ID = 2;
    private static final int USER_NOTIFICATION_SCHEMA_VERSION_FIELD_ID = 3;
    private static final int LOG_SCHEMA_VERSION_FIELD_ID = 4;
    private static final int EVENT_FAMILY_VERSIONS_COUNT_FIELD_ID = 5;
    private static final int PUBLIC_KEY_FIELD_ID = 6;
    private static final int ACCESS_TOKEN_FIELD_ID = 7;

    // User client sync fields
    private static final int USER_ATTACH_FIELD_ID = 0;
    private static final int ENDPOINT_ATTACH_FIELD_ID = 1;
    private static final int ENDPOINT_DETACH_FIELD_ID = 2;

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        if (buf.remaining() < MIN_SIZE_OF_MESSAGE_HEADER) {
            throw new PlatformEncDecException(MessageFormat.format("Message header is to small {} to be kaa binary message!",
                    buf.capacity()));
        }

        int protocolId = buf.getInt();
        if (protocolId != PROTOCOL_ID) {
            throw new PlatformEncDecException(MessageFormat.format("Unknown protocol id {}!", protocolId));
        }

        int protocolVersion = buf.getShort();
        if (protocolVersion < MIN_SUPPORTED_VERSION || protocolVersion > MAX_SUPPORTED_VERSION) {
            throw new PlatformEncDecException(MessageFormat.format("Can't decode data using protocol version {}!", protocolVersion));
        }

        short extensionsCount = buf.getShort();
        LOG.trace("received data for protocol id {} and version {} that contain {} extensions", protocolId, protocolVersion,
                extensionsCount);
        return parseExtensions(buf, protocolVersion, extensionsCount);
    }

    private ClientSync parseExtensions(ByteBuffer buf, int protocolVersion, short extensionsCount) throws PlatformEncDecException {
        ClientSync sync = new ClientSync();
        for (short extPos = 0; extPos < extensionsCount; extPos++) {
            if (buf.remaining() < MIN_SIZE_OF_EXTENSION_HEADER) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension header is to small. Available {}, current possition is {}!", buf.remaining(), buf.position()));
            }
            int extMetaData = buf.getInt();
            int type = extMetaData & 0xF000 >> 16;
            int options = extMetaData & 0x0FFF;
            int payloadLength = buf.getInt();
            if (buf.remaining() < payloadLength) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension payload is to small. Available {}, expected {} current possition is {}!", buf.remaining(),
                        payloadLength, buf.position()));
            }
            switch (type) {
            case META_DATA_EXTENSION_ID:
                parseClientSyncMetaData(sync, buf, options, payloadLength);
                break;
            case PROFILE_EXTENSION_ID:
                parseProfileClientSync(sync, buf, options, payloadLength);
                break;
            case USER_EXTENSION_ID:
                parseUserClientSync(sync, buf, options, payloadLength);
                break;
            case LOGGING_EXTENSION_ID:
                parseLogClientSync(sync, buf, options, payloadLength);
                break;
            default:
                break;
            }
        }
        return validate(sync);
    }

    private void parseClientSyncMetaData(ClientSync sync, ByteBuffer buf, int options, int payloadLength) throws PlatformEncDecException {
        sync.setRequestId(buf.getInt());
        ClientSyncMetaData md = new ClientSyncMetaData();
        if (hasOption(options, 0x01)) {
            md.setTimeout((long) buf.getInt());
        }
        if (hasOption(options, 0x02)) {
            md.setEndpointPublicKeyHash(getNewByteBuffer(buf, PUBLIC_KEY_HASH_SIZE));
        }
        if (hasOption(options, 0x04)) {
            md.setProfileHash(getNewByteBuffer(buf, PUBLIC_KEY_SIZE));
        }
        if (hasOption(options, 0x08)) {
            md.setApplicationToken(getUTF8String(buf));
        }
        sync.setClientSyncMetaData(md);
    }

    private void parseProfileClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;
        ProfileClientSync profileSync = new ProfileClientSync();
        profileSync.setEndpointPublicKey(getNewByteBuffer(buf, buf.getInt()));
        profileSync.setVersionInfo(new EndpointVersionInfo());
        while (buf.position() < payloadLimitPosition) {
            int fieldId = buf.get();
            // reading unused reserved field
            buf.get();
            switch (fieldId) {
            case CONF_SCHEMA_VERSION_FIELD_ID:
                profileSync.getVersionInfo().setConfigVersion(getIntFromUnsignedShort(buf));
                break;
            case PROFILE_SCHEMA_VERSION_FIELD_ID:
                profileSync.getVersionInfo().setProfileVersion(getIntFromUnsignedShort(buf));
                break;
            case SYSTEM_NOTIFICATION_SCHEMA_VERSION_FIELD_ID:
                profileSync.getVersionInfo().setSystemNfVersion(getIntFromUnsignedShort(buf));
                break;
            case USER_NOTIFICATION_SCHEMA_VERSION_FIELD_ID:
                profileSync.getVersionInfo().setUserNfVersion(getIntFromUnsignedShort(buf));
                break;
            case LOG_SCHEMA_VERSION_FIELD_ID:
                profileSync.getVersionInfo().setLogSchemaVersion(getIntFromUnsignedShort(buf));
                break;
            case EVENT_FAMILY_VERSIONS_COUNT_FIELD_ID:
                profileSync.getVersionInfo().setEventFamilyVersions(parseEventFamilyVersionList(buf, getIntFromUnsignedShort(buf)));
                break;
            case PUBLIC_KEY_FIELD_ID:
                profileSync.setEndpointPublicKey(getNewByteBuffer(buf, getIntFromUnsignedShort(buf)));
                break;
            case ACCESS_TOKEN_FIELD_ID:
                profileSync.setEndpointAccessToken(getUTF8String(buf));
            }
        }
        sync.setProfileSync(profileSync);
    }

    private void parseUserClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;
        UserClientSync userSync = new UserClientSync();
        while (buf.position() <= payloadLimitPosition) {
            int fieldId = buf.get();
            switch (fieldId) {
            case USER_ATTACH_FIELD_ID:
                userSync.setUserAttachRequest(parseUserAttachRequest(buf));
                break;
            case ENDPOINT_ATTACH_FIELD_ID:
                userSync.setEndpointAttachRequests(parseEndpointAttachRequests(buf));
                break;
            case ENDPOINT_DETACH_FIELD_ID:
                userSync.setEndpointDetachRequests(parseEndpointDetachRequests(buf));
                break;
            }
        }
        sync.setUserSync(userSync);
    }

    private void parseLogClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        LogClientSync logSync = new LogClientSync();
        logSync.setRequestId(String.valueOf(getIntFromUnsignedShort(buf)));
        int size = getIntFromUnsignedShort(buf);
        List<LogEntry> logs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            new LogEntry(getNewByteBuffer(buf, buf.getInt()));
        }
        logSync.setLogEntries(logs);
        sync.setLogSync(logSync);
    }

    private List<EndpointAttachRequest> parseEndpointAttachRequests(ByteBuffer buf) {
        // reserved
        buf.get();
        int count = getIntFromUnsignedShort(buf);
        List<EndpointAttachRequest> requests = new ArrayList<EndpointAttachRequest>(count);
        for (int i = 0; i < count; i++) {
            int requestId = getIntFromUnsignedShort(buf);
            String accessToken = getUTF8String(buf);
            requests.add(new EndpointAttachRequest(String.valueOf(requestId), accessToken));
        }
        return requests;
    }

    private List<EndpointDetachRequest> parseEndpointDetachRequests(ByteBuffer buf) {
        // reserved
        buf.get();
        int count = getIntFromUnsignedShort(buf);
        List<EndpointDetachRequest> requests = new ArrayList<EndpointDetachRequest>(count);
        for (int i = 0; i < count; i++) {
            int requestId = getIntFromUnsignedShort(buf);
            // reserved
            buf.getShort();
            requests.add(new EndpointDetachRequest(String.valueOf(requestId), Base64Util.encode(getNewByteArray(buf, PUBLIC_KEY_HASH_SIZE))));
        }
        return requests;
    }

    private UserAttachRequest parseUserAttachRequest(ByteBuffer buf) {
        int extIdLength = buf.get() & 0xFF;
        int tokenLength = getIntFromUnsignedShort(buf);
        String userExternalId = getUTF8String(buf, extIdLength);
        String userAccessToken = getUTF8String(buf, tokenLength);
        return new UserAttachRequest(userExternalId, userAccessToken);
    }

    private static List<EventClassFamilyVersionInfo> parseEventFamilyVersionList(ByteBuffer buf, int count) {
        List<EventClassFamilyVersionInfo> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int version = getIntFromUnsignedShort(buf);
            result.add(new EventClassFamilyVersionInfo(getUTF8String(buf), version));
        }
        return result;
    }

    private static int getIntFromUnsignedShort(ByteBuffer buf) {
        // handle unsigned integers from client
        return (int) buf.getChar();
    }

    private static boolean hasOption(int options, int option) {
        return (options & option) > 0;
    }

    private static String getUTF8String(ByteBuffer buf) {
        return getUTF8String(buf, buf.getInt());
    }

    private static String getUTF8String(ByteBuffer buf, int size) {
        String result = new String(getNewByteArray(buf, size), UTF8);
        handlePadding(buf, size);
        return result;
    }

    private static byte[] getNewByteArray(ByteBuffer buf, int size, boolean withPadding) {
        byte[] array = new byte[size];
        buf.get(array);
        if (withPadding) {
            handlePadding(buf, size);
        }
        return array;
    }

    private static void handlePadding(ByteBuffer buf, int size) {
        buf.position(buf.position() + size % PADDING_SIZE);
    }

    private static byte[] getNewByteArray(ByteBuffer buf, int size) {
        return getNewByteArray(buf, size, true);
    }

    private static ByteBuffer getNewByteBuffer(ByteBuffer buf, int size) {
        return getNewByteBuffer(buf, size, true);
    }

    private static ByteBuffer getNewByteBuffer(ByteBuffer buf, int size, boolean withPadding) {
        return ByteBuffer.wrap(getNewByteArray(buf, size, withPadding));
    }

    public static void main(String args[]) {
        int a = 60; /* 60 = 0011 1100 */
        int b = 13; /* 13 = 0000 1101 */
        int c = 0;

        c = a & b; /* 12 = 0000 1100 */
        System.out.println("a & b = " + c);

        c = a | b; /* 61 = 0011 1101 */
        System.out.println("a | b = " + c);

        c = a ^ b; /* 49 = 0011 0001 */
        System.out.println("a ^ b = " + c);

        c = ~a; /*-61 = 1100 0011 */
        System.out.println("~a = " + c);

        c = a << 2; /* 240 = 1111 0000 */
        System.out.println("a << 2 = " + c);

        c = a >> 2; /* 215 = 1111 */
        System.out.println("a >> 2  = " + c);

        c = a >>> 2; /* 215 = 0000 1111 */
        System.out.println("a >>> 2 = " + c);
    }

    private ClientSync validate(ClientSync sync) throws PlatformEncDecException {
        if (sync.getClientSyncMetaData() == null) {
            throw new PlatformEncDecException(MessageFormat.format("Input data does not have client sync meta data: {}!", sync));
        }
        return sync;
    }

    @Override
    public byte[] encode(ServerSync sync) throws PlatformEncDecException {
        // TODO Auto-generated method stub
        return null;
    }

}
