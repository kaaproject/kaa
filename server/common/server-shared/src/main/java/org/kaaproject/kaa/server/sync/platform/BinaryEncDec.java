/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.sync.platform;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.Event;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.EventListenersRequest;
import org.kaaproject.kaa.server.sync.EventListenersResponse;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.LogClientSync;
import org.kaaproject.kaa.server.sync.LogDeliveryStatus;
import org.kaaproject.kaa.server.sync.LogEntry;
import org.kaaproject.kaa.server.sync.LogServerSync;
import org.kaaproject.kaa.server.sync.Notification;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.NotificationServerSync;
import org.kaaproject.kaa.server.sync.NotificationType;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.ProfileServerSync;
import org.kaaproject.kaa.server.sync.RedirectServerSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SubscriptionCommand;
import org.kaaproject.kaa.server.sync.SubscriptionCommandType;
import org.kaaproject.kaa.server.sync.SubscriptionType;
import org.kaaproject.kaa.server.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.Topic;
import org.kaaproject.kaa.server.sync.TopicState;
import org.kaaproject.kaa.server.sync.UserAttachNotification;
import org.kaaproject.kaa.server.sync.UserAttachRequest;
import org.kaaproject.kaa.server.sync.UserAttachResponse;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.sync.UserDetachNotification;
import org.kaaproject.kaa.server.sync.UserServerSync;
import org.kaaproject.kaa.server.sync.bootstrap.BootstrapClientSync;
import org.kaaproject.kaa.server.sync.bootstrap.BootstrapServerSync;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolVersionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of {@link PlatformEncDec} that uses internal
 * binary protocol for data serialization.
 *
 * @author Andrew Shvayka
 *
 */
@KaaPlatformProtocol
public class BinaryEncDec implements PlatformEncDec {

    private static final int EVENT_SEQ_NUMBER_REQUEST_OPTION = 0x02;
    private static final int CONFIGURATION_HASH_OPTION = 0x02;
    private static final int CONFIGURATION_RESYNC_OPTION = 0x04;
    public static final short PROTOCOL_VERSION = 1;
    public static final int MIN_SUPPORTED_VERSION = 1;
    public static final int MAX_SUPPORTED_VERSION = 1;

    // General constants
    private static final Logger LOG = LoggerFactory.getLogger(BinaryEncDec.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
    private static final int DEFAULT_BUFFER_SIZE = 128;
    private static final int SIZE_OF_INT = 4;
    private static final int EXTENSIONS_COUNT_POSITION = 6;
    private static final int MIN_SIZE_OF_MESSAGE_HEADER = 8;
    private static final int MIN_SIZE_OF_EXTENSION_HEADER = 8;
    private static final byte SUCCESS = 0x00;

    static final int PADDING_SIZE = 4;
    // Options
    static final byte FAILURE = 0x01;
    static final byte RESYNC = 0x01;
    static final byte NOTHING = 0x00;

    private static final byte USER_SYNC_ENDPOINT_ID_OPTION = 0x01;
    private static final short EVENT_DATA_IS_EMPTY_OPTION = (short) 0x02;
    private static final int CLIENT_EVENT_DATA_IS_PRESENT_OPTION = 0x02;
    private static final int CLIENT_META_SYNC_SDK_TOKEN_OPTION = 0x08;
    private static final int CLIENT_META_SYNC_PROFILE_HASH_OPTION = 0x04;
    private static final int CLIENT_META_SYNC_KEY_HASH_OPTION = 0x02;
    private static final int CLIENT_META_SYNC_TIMEOUT_OPTION = 0x01;

    // Notification types
    static final byte SYSTEM = 0x00;
    static final byte CUSTOM = 0x01;
    // Subscription types
    static final byte MANDATORY = 0x00;
    static final byte OPTIONAL = 0x01;

    // Extension constants
    static final byte BOOTSTRAP_EXTENSION_ID = 0;
    static final byte META_DATA_EXTENSION_ID = 1;
    static final byte PROFILE_EXTENSION_ID = 2;
    static final byte USER_EXTENSION_ID = 3;
    static final byte LOGGING_EXTENSION_ID = 4;
    static final byte CONFIGURATION_EXTENSION_ID = 5;
    static final byte NOTIFICATION_EXTENSION_ID = 6;
    static final byte EVENT_EXTENSION_ID = 7;

    // Meta data constants
    private static final int PUBLIC_KEY_HASH_SIZE = 20;
    private static final int PROFILE_HASH_SIZE = 20;
    private static final int CONFIGURATION_HASH_SIZE = 20;
    private static final int TOPIC_LIST_HASH_SIZE = 20;

    // Profile client sync fields
    private static final byte CONF_SCHEMA_VERSION_FIELD_ID = 0;
    private static final byte PROFILE_SCHEMA_VERSION_FIELD_ID = 1;
    private static final byte SYSTEM_NOTIFICATION_SCHEMA_VERSION_FIELD_ID = 2;
    private static final byte USER_NOTIFICATION_SCHEMA_VERSION_FIELD_ID = 3;
    private static final byte LOG_SCHEMA_VERSION_FIELD_ID = 4;
    private static final byte EVENT_FAMILY_VERSIONS_COUNT_FIELD_ID = 5;
    private static final byte PUBLIC_KEY_FIELD_ID = 6;
    private static final byte ACCESS_TOKEN_FIELD_ID = 7;

    // User client sync fields
    private static final byte USER_ATTACH_FIELD_ID = 0;
    private static final byte ENDPOINT_ATTACH_FIELD_ID = 1;
    private static final byte ENDPOINT_DETACH_FIELD_ID = 2;

    // User server sync fields
    private static final byte USER_ATTACH_RESPONSE_FIELD_ID = 0;
    private static final byte USER_ATTACH_NOTIFICATION_FIELD_ID = 1;
    private static final byte USER_DETACH_NOTIFICATION_FIELD_ID = 2;
    private static final byte ENDPOINT_ATTACH_RESPONSE_FIELD_ID = 3;
    private static final byte ENDPOINT_DETACH_RESPONSE_FIELD_ID = 4;

    // Event client sync fields
    private static final byte EVENT_LISTENERS_FIELD_ID = 0;
    private static final byte EVENT_LIST_FIELD_ID = 1;

    // Event server sync fields
    private static final byte EVENT_LISTENERS_RESPONSE_FIELD_ID = 0;
    private static final byte EVENT_LIST_RESPONSE_FIELD_ID = 1;

    // Notification client sync fields
    private static final byte NF_TOPIC_STATES_FIELD_ID = 0;
    private static final byte NF_UNICAST_LIST_FIELD_ID = 1;
    private static final byte NF_SUBSCRIPTION_ADD_FIELD_ID = 2;
    private static final byte NF_SUBSCRIPTION_REMOVE_FIELD_ID = 3;

    // Notification server sync fields
    private static final byte NF_TOPICS_FIELD_ID = 0;
    private static final byte NF_NOTIFICATIONS_FIELD_ID = 1;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.
     * PlatformEncDec#getId()
     */
    @Override
    public int getId() {
        return Constants.KAA_PLATFORM_PROTOCOL_BINARY_ID;
    }

    @Override
    public ClientSync decode(byte[] data) throws PlatformEncDecException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Decoding binary data {}", Arrays.toString(data));
        }
        ByteBuffer buf = ByteBuffer.wrap(data);
        if (buf.remaining() < MIN_SIZE_OF_MESSAGE_HEADER) {
            throw new PlatformEncDecException(MessageFormat.format("Message header is to small {0} to be kaa binary message!",
                    buf.capacity()));
        }

        int protocolId = buf.getInt();
        if (protocolId != getId()) {
            throw new PlatformEncDecException(MessageFormat.format("Unknown protocol id {0}!", protocolId));
        }

        int protocolVersion = getIntFromUnsignedShort(buf);
        if (protocolVersion < MIN_SUPPORTED_VERSION || protocolVersion > MAX_SUPPORTED_VERSION) {
            throw new PlatformEncDecException(MessageFormat.format("Can't decode data using protocol version {0}!", protocolVersion));
        }

        int extensionsCount = getIntFromUnsignedShort(buf);
        LOG.trace("received data for protocol id {} and version {} that contain {} extensions", protocolId, protocolVersion,
                extensionsCount);
        ClientSync sync = parseExtensions(buf, protocolVersion, extensionsCount);
        sync.setUseConfigurationRawSchema(false);
        LOG.trace("Decoded binary data {}", sync);
        return sync;
    }

    @Override
    public byte[] encode(ServerSync sync) throws PlatformEncDecException {
        LOG.trace("Encoding server sync {}", sync);
        GrowingByteBuffer buf = new GrowingByteBuffer(DEFAULT_BUFFER_SIZE);
        buf.putInt(getId());
        buf.putShort(PROTOCOL_VERSION);
        buf.putShort(NOTHING); // will be updated later
        encodeMetaData(buf, sync);
        short extensionCount = 1;

        if (sync.getBootstrapSync() != null) {
            encode(buf, sync.getBootstrapSync());
            extensionCount++;
        }
        if (sync.getProfileSync() != null) {
            encode(buf, sync.getProfileSync());
            extensionCount++;
        }
        if (sync.getUserSync() != null) {
            encode(buf, sync.getUserSync());
            extensionCount++;
        }
        if (sync.getLogSync() != null) {
            encode(buf, sync.getLogSync());
            extensionCount++;
        }
        if (sync.getConfigurationSync() != null) {
            encode(buf, sync.getConfigurationSync());
            extensionCount++;
        }
        if (sync.getNotificationSync() != null) {
            encode(buf, sync.getNotificationSync());
            extensionCount++;
        }
        if (sync.getEventSync() != null) {
            encode(buf, sync.getEventSync());
            extensionCount++;
        }

        if (sync.getRedirectSync() != null) {
            encode(buf, sync.getRedirectSync());
            extensionCount++;
        }

        buf.putShort(EXTENSIONS_COUNT_POSITION, extensionCount);
        byte[] result = buf.toByteArray();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Encoded binary data {}", result);
        }
        return result;
    }

    private void buildExtensionHeader(GrowingByteBuffer buf, short extensionId, byte optionA, byte optionB, int length) {
        buf.putShort(extensionId);
        buf.put(optionA);
        buf.put(optionB);
        buf.putInt(length);
    }

    private void encodeMetaData(GrowingByteBuffer buf, ServerSync sync) {
        buildExtensionHeader(buf, META_DATA_EXTENSION_ID, NOTHING, NOTHING, 8);
        buf.putInt(sync.getRequestId());
        buf.putInt(sync.getStatus().ordinal());
    }

    private void encode(GrowingByteBuffer buf, BootstrapServerSync bootstrapSync) {
        buildExtensionHeader(buf, BOOTSTRAP_EXTENSION_ID, NOTHING, NOTHING, 0);
        int extPosition = buf.position();
        buf.putShort((short) bootstrapSync.getRequestId());
        buf.putShort((short) bootstrapSync.getProtocolList().size());
        for (ProtocolConnectionData data : bootstrapSync.getProtocolList()) {
            buf.putInt(data.getAccessPointId());
            buf.putInt(data.getProtocolId());
            buf.putShort((short) data.getProtocolVersion());
            buf.putShort((short) data.getConnectionData().length);
            put(buf, data.getConnectionData());
        }
        buf.putInt(extPosition - SIZE_OF_INT, buf.position() - extPosition);
    }

    private void encode(GrowingByteBuffer buf, ProfileServerSync profileSync) {
        buildExtensionHeader(buf, PROFILE_EXTENSION_ID, NOTHING,
                             (profileSync.getResponseStatus() == SyncResponseStatus.RESYNC ? RESYNC : NOTHING), 0);
    }

    private void encode(GrowingByteBuffer buf, UserServerSync userSync) {
        buildExtensionHeader(buf, USER_EXTENSION_ID, NOTHING, NOTHING, 0);
        int extPosition = buf.position();
        if (userSync.getUserAttachResponse() != null) {
            UserAttachResponse uaResponse = userSync.getUserAttachResponse();
            buf.put(USER_ATTACH_RESPONSE_FIELD_ID);
            buf.put(NOTHING);
            buf.put(uaResponse.getResult() == SyncStatus.SUCCESS ? SUCCESS : FAILURE);
            buf.put(NOTHING);

            if (uaResponse.getResult() != SyncStatus.SUCCESS) {
                buf.putShort((short) (uaResponse.getErrorCode() != null ? uaResponse.getErrorCode().ordinal() : 0));
                if (uaResponse.getErrorReason() != null) {
                    byte[] data = uaResponse.getErrorReason().getBytes(UTF8);
                    buf.putShort((short) data.length);
                    put(buf, data);
                } else {
                    buf.putShort((short) 0);
                }
            }
        }
        if (userSync.getUserAttachNotification() != null) {
            UserAttachNotification nf = userSync.getUserAttachNotification();
            buf.put(USER_ATTACH_NOTIFICATION_FIELD_ID);
            buf.put((byte) nf.getUserExternalId().length());
            buf.putShort((short) nf.getEndpointAccessToken().length());
            putUTF(buf, nf.getUserExternalId());
            putUTF(buf, nf.getEndpointAccessToken());
        }
        if (userSync.getUserDetachNotification() != null) {
            UserDetachNotification nf = userSync.getUserDetachNotification();
            buf.put(USER_DETACH_NOTIFICATION_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) nf.getEndpointAccessToken().length());
            putUTF(buf, nf.getEndpointAccessToken());
        }
        if (userSync.getEndpointAttachResponses() != null) {
            buf.put(ENDPOINT_ATTACH_RESPONSE_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) userSync.getEndpointAttachResponses().size());
            for (EndpointAttachResponse response : userSync.getEndpointAttachResponses()) {
                buf.put(response.getResult() == SyncStatus.SUCCESS ? SUCCESS : FAILURE);
                if (response.getEndpointKeyHash() != null) {
                    buf.put(USER_SYNC_ENDPOINT_ID_OPTION);
                } else {
                    buf.put(NOTHING);
                }
                buf.putShort((short) response.getRequestId());
                if (response.getEndpointKeyHash() != null) {
                    put(buf, Base64Util.decode(response.getEndpointKeyHash()));
                }
            }
        }
        if (userSync.getEndpointDetachResponses() != null) {
            buf.put(ENDPOINT_DETACH_RESPONSE_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) userSync.getEndpointDetachResponses().size());
            for (EndpointDetachResponse response : userSync.getEndpointDetachResponses()) {
                buf.put(response.getResult() == SyncStatus.SUCCESS ? SUCCESS : FAILURE);
                buf.put(NOTHING);
                buf.putShort((short) response.getRequestId());
            }
        }
        buf.putInt(extPosition - SIZE_OF_INT, buf.position() - extPosition);
    }

    private void encode(GrowingByteBuffer buf, LogServerSync logSync) {
        List<LogDeliveryStatus> statusList = logSync.getDeliveryStatuses();

        int extensionSize = 4;
        if (statusList != null) {
            extensionSize += 4 * statusList.size();
        }

        buildExtensionHeader(buf, LOGGING_EXTENSION_ID, NOTHING, NOTHING, extensionSize);

        if (statusList != null && !statusList.isEmpty()) {
            buf.putInt(statusList.size());

            for (LogDeliveryStatus status : statusList) {
                buf.putShort((short) status.getRequestId());
                buf.put(status.getResult() == SyncStatus.SUCCESS ? SUCCESS : FAILURE);
                buf.put(status.getErrorCode() != null ? (byte) status.getErrorCode().ordinal() : NOTHING);
            }
        } else {
            buf.putInt(0);
        }
    }

    private void encode(GrowingByteBuffer buf, ConfigurationServerSync configurationSync) {
        int option = 0;
        boolean confSchemaPresent = configurationSync.getConfSchemaBody() != null;
        boolean confBodyPresent = configurationSync.getConfDeltaBody() != null;
        if (confSchemaPresent) {
            option |= 0x01;
        }
        if (confBodyPresent) {
            option |= 0x02;
        }
        buildExtensionHeader(buf, CONFIGURATION_EXTENSION_ID, NOTHING, (byte) option, 0);
        int extPosition = buf.position();

        if (confSchemaPresent) {
            buf.putInt(configurationSync.getConfSchemaBody().array().length);
        }
        if (confBodyPresent) {
            buf.putInt(configurationSync.getConfDeltaBody().array().length);
        }
        if (confSchemaPresent) {
            put(buf, configurationSync.getConfSchemaBody().array());
        }
        if (confBodyPresent) {
            put(buf, configurationSync.getConfDeltaBody().array());
        }

        buf.putInt(extPosition - SIZE_OF_INT, buf.position() - extPosition);
    }

    private void encode(GrowingByteBuffer buf, NotificationServerSync notificationSync) {
        buildExtensionHeader(buf, NOTIFICATION_EXTENSION_ID, NOTHING, NOTHING, 0);
        int extPosition = buf.position();

        SyncResponseStatus status = notificationSync.getResponseStatus();
        switch (status) {
        case NO_DELTA:
            buf.putInt(0);
            break;
        case DELTA:
            buf.putInt(1);
            break;
        case RESYNC:
            buf.putInt(2);
            break;
        }
        if (notificationSync.getAvailableTopics() != null) {
            buf.put(NF_TOPICS_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) notificationSync.getAvailableTopics().size());
            for (Topic t : notificationSync.getAvailableTopics()) {
                buf.putLong(t.getIdAsLong());
                buf.put(t.getSubscriptionType() == SubscriptionType.MANDATORY ? MANDATORY : OPTIONAL);
                buf.put(NOTHING);
                buf.putShort((short) t.getName().getBytes(UTF8).length);
                putUTF(buf, t.getName());
            }
        }
        if (notificationSync.getNotifications() != null) {
            buf.put(NF_NOTIFICATIONS_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) notificationSync.getNotifications().size());
            for (Notification nf : notificationSync.getNotifications()) {
                buf.putInt((nf.getSeqNumber() != null) ? nf.getSeqNumber() : 0);
                buf.put(nf.getType() == NotificationType.SYSTEM ? SYSTEM : CUSTOM);
                buf.put(NOTHING);
                buf.putShort(nf.getUid() != null ? (short) nf.getUid().length() : (short) 0);
                buf.putInt(nf.getBody().array().length);
                long topicId = nf.getTopicId() != null ? nf.getTopicIdAsLong() : 0l;
                buf.putLong(topicId);
                putUTF(buf, nf.getUid());
                put(buf, nf.getBody().array());
            }
        }

        buf.putInt(extPosition - SIZE_OF_INT, buf.position() - extPosition);
    }

    private void encode(GrowingByteBuffer buf, EventServerSync eventSync) {
        byte option = 0;
        if (eventSync.getEventSequenceNumberResponse() != null) {
            option = 1;
        }
        buildExtensionHeader(buf, EVENT_EXTENSION_ID, NOTHING, option, 0);
        int extPosition = buf.position();

        if (eventSync.getEventSequenceNumberResponse() != null) {
            buf.putInt(eventSync.getEventSequenceNumberResponse().getSeqNum());
        }

        if (eventSync.getEventListenersResponses() != null && !eventSync.getEventListenersResponses().isEmpty()) {
            buf.put(EVENT_LISTENERS_RESPONSE_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) eventSync.getEventListenersResponses().size());
            for (EventListenersResponse response : eventSync.getEventListenersResponses()) {
                buf.putShort((short) response.getRequestId());
                buf.putShort(response.getResult() == SyncStatus.SUCCESS ? SUCCESS : FAILURE);
                if (response.getListeners() != null) {
                    buf.putInt(response.getListeners().size());
                    for (String listener : response.getListeners()) {
                        put(buf, Base64Util.decode(listener));
                    }
                } else {
                    buf.putInt(0);
                }
            }
        }
        if (eventSync.getEvents() != null) {
            buf.put(EVENT_LIST_RESPONSE_FIELD_ID);
            buf.put(NOTHING);
            buf.putShort((short) eventSync.getEvents().size());
            for (Event event : eventSync.getEvents()) {
                boolean eventDataIsEmpty = event.getEventData() == null || event.getEventData().array().length == 0;
                if (!eventDataIsEmpty) {
                    buf.putShort(EVENT_DATA_IS_EMPTY_OPTION);
                } else {
                    buf.putShort(NOTHING);
                }
                buf.putShort((short) event.getEventClassFQN().length());
                if (!eventDataIsEmpty) {
                    buf.putInt(event.getEventData().array().length);
                }
                buf.put(Base64Util.decode(event.getSource()));
                putUTF(buf, event.getEventClassFQN());
                if (!eventDataIsEmpty) {
                    put(buf, event.getEventData().array());
                }
            }
        }

        buf.putInt(extPosition - SIZE_OF_INT, buf.position() - extPosition);
    }

    private void encode(GrowingByteBuffer buf, RedirectServerSync redirectSync) {
        buildExtensionHeader(buf, EVENT_EXTENSION_ID, NOTHING, NOTHING, 4);
        buf.putInt(redirectSync.getAccessPointId());
    }

    public void putUTF(GrowingByteBuffer buf, String str) {
        if (str != null) {
            put(buf, str.getBytes(UTF8));
        }
    }

    private void put(GrowingByteBuffer buf, byte[] data) {
        buf.put(data);
        int padding = data.length % BinaryEncDec.PADDING_SIZE;
        if (padding > 0) {
            padding = PADDING_SIZE - padding;
            for (int i = 0; i < padding; i++) {
                buf.put(NOTHING);
            }
        }
    }

    private ClientSync parseExtensions(ByteBuffer buf, int protocolVersion, int extensionsCount) throws PlatformEncDecException {
        ClientSync sync = new ClientSync();
        for (short extPos = 0; extPos < extensionsCount; extPos++) {
            if (buf.remaining() < MIN_SIZE_OF_EXTENSION_HEADER) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension header is to small. Available {0}, current possition is {1}!", buf.remaining(), buf.position()));
            }
            short type = buf.getShort();
            int options = buf.getShort();
            int payloadLength = buf.getInt();
            if (buf.remaining() < payloadLength) {
                throw new PlatformEncDecException(MessageFormat.format(
                        "Extension payload is to small. Available {0}, expected {1} current possition is {2}!", buf.remaining(),
                        payloadLength, buf.position()));
            }
            switch (type) {
            case BOOTSTRAP_EXTENSION_ID:
                parseBootstrapClientSync(sync, buf, options, payloadLength);
                break;
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
        if (hasOption(options, CLIENT_META_SYNC_TIMEOUT_OPTION)) {
            md.setTimeout((long) buf.getInt());
        }
        if (hasOption(options, CLIENT_META_SYNC_KEY_HASH_OPTION)) {
            md.setEndpointPublicKeyHash(getNewByteBuffer(buf, PUBLIC_KEY_HASH_SIZE));
        }
        if (hasOption(options, CLIENT_META_SYNC_PROFILE_HASH_OPTION)) {
            md.setProfileHash(getNewByteBuffer(buf, PROFILE_HASH_SIZE));
        }
        if (hasOption(options, CLIENT_META_SYNC_SDK_TOKEN_OPTION)) {
            md.setSdkToken(getUTF8String(buf, Constants.SDK_TOKEN_SIZE));
        }
        sync.setClientSyncMetaData(md);
    }

    private void parseBootstrapClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int requestId = buf.getShort();
        int protocolCount = buf.getShort();
        List<ProtocolVersionId> keys = new ArrayList<>(protocolCount);
        for (int i = 0; i < protocolCount; i++) {
            keys.add(new ProtocolVersionId(buf.getInt(), buf.getShort()));
            // reserved
            buf.getShort();
        }
        sync.setBootstrapSync(new BootstrapClientSync(requestId, keys));
    }

    private void parseProfileClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;
        ProfileClientSync profileSync = new ProfileClientSync();
        profileSync.setProfileBody(getNewByteBuffer(buf, buf.getInt()));
        while (buf.position() < payloadLimitPosition) {
            byte fieldId = buf.get();
            // reading unused reserved field
            buf.get();
            switch (fieldId) {
            case PUBLIC_KEY_FIELD_ID:
                profileSync.setEndpointPublicKey(getNewByteBuffer(buf, getIntFromUnsignedShort(buf)));
                break;
            case ACCESS_TOKEN_FIELD_ID:
                profileSync.setEndpointAccessToken(getUTF8String(buf));
                break;
            default:
                break;
            }
        }
        sync.setProfileSync(profileSync);
    }

    private void parseUserClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;
        UserClientSync userSync = new UserClientSync();
        while (buf.position() < payloadLimitPosition) {
            byte fieldId = buf.get();
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
            default:
                break;
            }
        }
        sync.setUserSync(userSync);
    }

    private void parseLogClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        LogClientSync logSync = new LogClientSync();
        logSync.setRequestId(getIntFromUnsignedShort(buf));
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
        if (hasOption(options, CONFIGURATION_HASH_OPTION)) {
            confSync.setConfigurationHash(getNewByteBuffer(buf, CONFIGURATION_HASH_SIZE));
        }
        if (hasOption(options, CONFIGURATION_RESYNC_OPTION)) {
            confSync.setResyncOnly(true);
        }
        sync.setConfigurationSync(confSync);
    }

    private void parseEventClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        EventClientSync eventSync = new EventClientSync();
        if (hasOption(options, EVENT_SEQ_NUMBER_REQUEST_OPTION)) {
            eventSync.setSeqNumberRequest(true);
        }
        int payloadLimitPosition = buf.position() + payloadLength;
        while (buf.position() < payloadLimitPosition) {
            byte fieldId = buf.get();
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

    private void  parseNotificationClientSync(ClientSync sync, ByteBuffer buf, int options, int payloadLength) {
        int payloadLimitPosition = buf.position() + payloadLength;

        NotificationClientSync nfSync = new NotificationClientSync();
        nfSync.setTopicListHash(buf.getInt());
        while (buf.position() < payloadLimitPosition) {
            byte fieldId = buf.get();
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
        sync.setNotificationSync(nfSync);
    }

    private void parseSubscriptionCommands(NotificationClientSync nfSync, ByteBuffer buf, boolean add) {
        int count = getIntFromUnsignedShort(buf);
        if (nfSync.getSubscriptionCommands() == null) {
            nfSync.setSubscriptionCommands(new ArrayList<SubscriptionCommand>());
        }
        SubscriptionCommandType subscriptionType = add ? SubscriptionCommandType.ADD : SubscriptionCommandType.REMOVE;
        List<SubscriptionCommand> commands = new ArrayList<SubscriptionCommand>();
        for (int i = 0; i < count; i++) {
            long topicId = buf.getLong();
            commands.add(new SubscriptionCommand(topicId, subscriptionType));
        }
        nfSync.getSubscriptionCommands().addAll(commands);
    }

    private List<TopicState> parseTopicStates(ByteBuffer buf) {
        int count = getIntFromUnsignedShort(buf);
        List<TopicState> topicStates = new ArrayList<TopicState>(count);
        for (int i = 0; i < count; i++) {
            long topicId = buf.getLong();
            int seqNumber = buf.getInt();
            topicStates.add(new TopicState(topicId, seqNumber));
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
            requests.add(new EventListenersRequest(requestId, fqns));
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
            int dataSize = 0;
            if (hasOption(eventOptions, CLIENT_EVENT_DATA_IS_PRESENT_OPTION)) {
                dataSize = buf.getInt();
            }
            if (hasOption(eventOptions, 0x01)) {
                event.setTarget(Base64Util.encode(getNewByteArray(buf, PUBLIC_KEY_HASH_SIZE)));
            }
            event.setEventClassFQN(getUTF8String(buf, fqnLength));
            if (dataSize > 0) {
                event.setEventData(getNewByteBuffer(buf, dataSize));
            } else {
                event.setEventData(EMPTY_BUFFER);
            }
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
            requests.add(new EndpointAttachRequest(requestId, accessToken));
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
            requests.add(new EndpointDetachRequest(requestId, Base64Util.encode(getNewByteArray(buf, PUBLIC_KEY_HASH_SIZE))));
        }
        return requests;
    }

    private UserAttachRequest parseUserAttachRequest(ByteBuffer buf) {
        int extIdLength = buf.get() & 0xFF;
        int tokenLength = getIntFromUnsignedShort(buf);
        int verifierTokenLength = getIntFromUnsignedShort(buf);
        // reserved
        buf.getShort();
        String userExternalId = getUTF8String(buf, extIdLength);
        String userAccessToken = getUTF8String(buf, tokenLength);
        String userVerifierToken = getUTF8String(buf, verifierTokenLength);
        return new UserAttachRequest(userVerifierToken, userExternalId, userAccessToken);
    }

    private static int getIntFromUnsignedShort(ByteBuffer buf) {
        // handle unsigned integers from client
        return buf.getChar();
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
        if (padding > 0) {
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
}
