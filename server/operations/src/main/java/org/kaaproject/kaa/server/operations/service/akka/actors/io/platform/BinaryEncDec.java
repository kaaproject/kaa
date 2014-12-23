package org.kaaproject.kaa.server.operations.service.akka.actors.io.platform;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.operations.pojo.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointVersionInfo;
import org.kaaproject.kaa.server.operations.pojo.sync.Event;
import org.kaaproject.kaa.server.operations.pojo.sync.EventClassFamilyVersionInfo;
import org.kaaproject.kaa.server.operations.pojo.sync.EventClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.EventListenersRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.LogClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.LogEntry;
import org.kaaproject.kaa.server.operations.pojo.sync.NotificationClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ProfileClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionCommand;
import org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionCommandType;
import org.kaaproject.kaa.server.operations.pojo.sync.TopicState;
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
    
    public static final int PROTOCOL_ID = 0x3553C66F; // TODO: update value of constant
    public static final int MIN_SUPPORTED_VERSION = 1;
    public static final int MAX_SUPPORTED_VERSION = 1;

    // Extension constants
    static final int META_DATA_EXTENSION_ID = 1;
    static final int PROFILE_EXTENSION_ID = 2;
    static final int USER_EXTENSION_ID = 3;
    static final int LOGGING_EXTENSION_ID = 4;
    static final int CONFIGURATION_EXTENSION_ID = 5;
    static final int NOTIFICATION_EXTENSION_ID = 6;
    static final int EVENT_EXTENSION_ID = 7;

    // Meta data constants
    private static final int PUBLIC_KEY_HASH_SIZE = 20;
    private static final int PROFILE_HASH_SIZE = 20;
    private static final int CONFIGURATION_HASH_SIZE = 20;
    private static final int TOPIC_LIST_HASH_SIZE = 20;

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

    // Event client sync fields
    private static final int EVENT_LISTENERS_FIELD_ID = 0;
    private static final int EVENT_LIST_FIELD_ID = 1;

    // Notification client sync fields
    private static final int NF_TOPIC_STATES_FIELD_ID = 0;
    private static final int NF_UNICAST_LIST_FIELD_ID = 1;
    private static final int NF_SUBSCRIPTION_ADD_FIELD_ID = 2;
    private static final int NF_SUBSCRIPTION_REMOVE_FIELD_ID = 3;

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        if (buf.remaining() < MIN_SIZE_OF_MESSAGE_HEADER) {
            throw new PlatformEncDecException(MessageFormat.format("Message header is to small {0} to be kaa binary message!",
                    buf.capacity()));
        }

        int protocolId = buf.getInt();
        if (protocolId != PROTOCOL_ID) {
            throw new PlatformEncDecException(MessageFormat.format("Unknown protocol id {0}!", protocolId));
        }

        int protocolVersion = getIntFromUnsignedShort(buf);
        if (protocolVersion < MIN_SUPPORTED_VERSION || protocolVersion > MAX_SUPPORTED_VERSION) {
            throw new PlatformEncDecException(MessageFormat.format("Can't decode data using protocol version {0}!", protocolVersion));
        }

        int extensionsCount = getIntFromUnsignedShort(buf);
        LOG.trace("received data for protocol id {} and version {} that contain {} extensions", protocolId, protocolVersion,
                extensionsCount);
        return parseExtensions(buf, protocolVersion, extensionsCount);
    }
    
    private ClientSync parseExtensions(ByteBuffer buf, int protocolVersion, int extensionsCount) throws PlatformEncDecException {
        ClientSync sync = new ClientSync();
        for (short extPos = 0; extPos < extensionsCount; extPos++) {
            if (buf.remaining() < MIN_SIZE_OF_EXTENSION_HEADER) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension header is to small. Available {0}, current possition is {1}!", buf.remaining(), buf.position()));
            }
            int extMetaData = buf.getInt();
            int type = (extMetaData & 0xFF000000) >> 24;
            int options = extMetaData & 0x00FFFFFF;
            int payloadLength = buf.getInt();
            if (buf.remaining() < payloadLength) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension payload is to small. Available {0}, expected {1} current possition is {2}!", buf.remaining(),
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
            case CONFIGURATION_EXTENSION_ID:
                parseConfigurationClientSync(sync, buf, options, payloadLength);
                break;
            case NOTIFICATION_EXTENSION_ID:
                parseNotificationClientSync(sync, buf, options, payloadLength);
                break;
            case EVENT_EXTENSION_ID:
                parseEventClientSync(sync, buf, options, payloadLength);
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
            md.setProfileHash(getNewByteBuffer(buf, PROFILE_HASH_SIZE));
        }
        if (hasOption(options, 0x08)) {
            md.setApplicationToken(getUTF8String(buf, buf.getInt()));
        }
        sync.setClientSyncMetaData(md);
    }

    private void parseProfileClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;
        ProfileClientSync profileSync = new ProfileClientSync();
        profileSync.setProfileBody(getNewByteBuffer(buf, buf.getInt()));
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
        while (buf.position() < payloadLimitPosition) {
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
            logs.add(new LogEntry(getNewByteBuffer(buf, buf.getInt())));
        }
        logSync.setLogEntries(logs);
        sync.setLogSync(logSync);
    }

    private void parseConfigurationClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        ConfigurationClientSync confSync = new ConfigurationClientSync();
        confSync.setAppStateSeqNumber(buf.getInt());
        if (hasOption(options, 0x02)) {
            confSync.setConfigurationHash(getNewByteBuffer(buf, CONFIGURATION_HASH_SIZE));
        }
        sync.setConfigurationSync(confSync);
    }

    private void parseEventClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        EventClientSync eventSync = new EventClientSync();
        if (hasOption(options, 0x02)) {
            eventSync.setSeqNumberRequest(true);
        }
        int payloadLimitPosition = buf.position() + payloadLength;
        while (buf.position() < payloadLimitPosition) {
            int fieldId = buf.get();
            // reading unused reserved field
            buf.get();
            switch (fieldId) {
            case EVENT_LISTENERS_FIELD_ID:
                eventSync.setEventListenersRequests(parseListenerRequests(buf));
                break;
            case EVENT_LIST_FIELD_ID:
                eventSync.setEvents(parseEvents(buf));
                break;
            default:
                break;
            }
        }
        sync.setEventSync(eventSync);
    }

    private void parseNotificationClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength - TOPIC_LIST_HASH_SIZE;
        NotificationClientSync nfSync = new NotificationClientSync();
        nfSync.setAppStateSeqNumber(buf.getInt());
        while (buf.position() < payloadLimitPosition) {
            int fieldId = buf.get();
            // reading unused reserved field
            buf.get();
            switch (fieldId) {
            case NF_TOPIC_STATES_FIELD_ID:
                nfSync.setTopicStates(parseTopicStates(buf));
                break;
            case NF_UNICAST_LIST_FIELD_ID:
                nfSync.setAcceptedUnicastNotifications(parseUnicastIds(buf));
                break;
            case NF_SUBSCRIPTION_ADD_FIELD_ID:
                parseSubscriptionCommands(nfSync, buf, true);
                break;
            case NF_SUBSCRIPTION_REMOVE_FIELD_ID:
                parseSubscriptionCommands(nfSync, buf, false);
                break;
            }
        }
        nfSync.setTopicListHash(getNewByteBuffer(buf, TOPIC_LIST_HASH_SIZE));
        sync.setNotificationSync(nfSync);
    }

    private void parseSubscriptionCommands(NotificationClientSync nfSync, ByteBuffer buf, boolean add) {
        int count = getIntFromUnsignedShort(buf);
        if (nfSync.getSubscriptionCommands() == null) {
            nfSync.setSubscriptionCommands(new ArrayList<SubscriptionCommand>());
        }
        List<SubscriptionCommand> commands = new ArrayList<SubscriptionCommand>();
        for (int i = 0; i < count; i++) {
            long topicId = buf.getLong();
            commands.add(new SubscriptionCommand(String.valueOf(topicId), add ? SubscriptionCommandType.ADD
                    : SubscriptionCommandType.REMOVE));
        }
        nfSync.getSubscriptionCommands().addAll(commands);
    }

    private List<TopicState> parseTopicStates(ByteBuffer buf) {
        int count = getIntFromUnsignedShort(buf);
        List<TopicState> topicStates = new ArrayList<TopicState>(count);
        for (int i = 0; i < count; i++) {
            long topicId = buf.getLong();
            int seqNumber = buf.getInt();
            topicStates.add(new TopicState(String.valueOf(topicId), seqNumber));
        }
        return topicStates;
    }

    private List<String> parseUnicastIds(ByteBuffer buf) {
        int count = getIntFromUnsignedShort(buf);
        List<String> uids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int uidLength = buf.getInt();
            uids.add(getUTF8String(buf, uidLength));
        }
        return uids;
    }

    private List<EventListenersRequest> parseListenerRequests(ByteBuffer buf) {
        int requestsCount = getIntFromUnsignedShort(buf);
        List<EventListenersRequest> requests = new ArrayList<>(requestsCount);
        for (int i = 0; i < requestsCount; i++) {
            int requestId = getIntFromUnsignedShort(buf);
            int fqnCount = getIntFromUnsignedShort(buf);
            List<String> fqns = new ArrayList<>(fqnCount);
            for (int j = 0; j < fqnCount; j++) {
                int fqnLength = getIntFromUnsignedShort(buf);
                // reserved
                buf.getShort();
                fqns.add(getUTF8String(buf, fqnLength));
            }
            requests.add(new EventListenersRequest(String.valueOf(requestId), fqns));
        }
        return requests;
    }

    private List<Event> parseEvents(ByteBuffer buf) {
        int eventsCount = getIntFromUnsignedShort(buf);
        List<Event> events = new ArrayList<>(eventsCount);
        for (int i = 0; i < eventsCount; i++) {
            Event event = new Event();
            event.setSeqNum(buf.getInt());
            int eventOptions = getIntFromUnsignedShort(buf);
            int fqnLength = getIntFromUnsignedShort(buf);
            int dataSize = buf.getInt();
            if (hasOption(eventOptions, 0x01)) {
                event.setTarget(Base64Util.encode(getNewByteArray(buf, PUBLIC_KEY_HASH_SIZE)));
            }
            event.setEventClassFQN(getUTF8String(buf, fqnLength));
            event.setEventData(getNewByteBuffer(buf, dataSize));
            events.add(event);
        }
        return events;
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
        return getUTF8String(buf, getIntFromUnsignedShort(buf));
    }

    private static String getUTF8String(ByteBuffer buf, int size) {
        return new String(getNewByteArray(buf, size), UTF8);
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
        int padding = size % PADDING_SIZE;
        if(padding > 0){
            buf.position(buf.position() + (PADDING_SIZE - padding));
        }
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

    private ClientSync validate(ClientSync sync) throws PlatformEncDecException {
        if (sync.getClientSyncMetaData() == null) {
            throw new PlatformEncDecException(MessageFormat.format("Input data does not have client sync meta data: {0}!", sync));
        }
        return sync;
    }

    @Override
    public byte[] encode(ServerSync sync) throws PlatformEncDecException {
        // TODO Auto-generated method stub
        return null;
    }

}
