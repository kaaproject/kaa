package org.kaaproject.kaa.client.plugin;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.DefaultOperationDataProcessor;
import org.kaaproject.kaa.client.context.TransportContext;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.EntityMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.Message;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.VoidMessage;
import org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1Plugin;
import org.kaaproject.kaa.client.plugin.messaging.ext1.MethodAListener;
import org.kaaproject.kaa.client.plugin.messaging.ext1.MethodBListener;
import org.kaaproject.kaa.client.plugin.messaging.ext1.MethodCListener;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.ExtensionSync;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class MessagingPluginTest {

    private static final int MESSAGE_COUNT_SIZE = 4;
    private static final int DEFAULT_ID = 42;

    private static final short METHOD_1_ID = 1;
    private static final short METHOD_2_ID = 2;
    private static final short METHOD_3_ID = 3;

    private final AvroByteArrayConverter<ClassA> avroConverterA = new AvroByteArrayConverter<>(ClassA.class);
    private final AvroByteArrayConverter<ClassC> avroConverterC = new AvroByteArrayConverter<>(ClassC.class);

    @Test
    public void testMessagingPlugin() throws Exception {
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor();
        Messaging1Plugin pluginInstance = new Messaging1Plugin();

        MethodAListener aListener = Mockito.mock(MethodAListener.class);
        MethodBListener bListener = Mockito.mock(MethodBListener.class);
        MethodCListener cListener = Mockito.mock(MethodCListener.class);
        ExecutorService executor = Mockito.mock(ExecutorService.class);
        PluginContext context = Mockito.mock(PluginContext.class);

        Mockito.when(context.getCallbackExecutor()).thenReturn(executor);
        implementAsDirectExecutor(executor);

        pluginInstance.init(context);
        pluginInstance.setMethodAListener(aListener);
        pluginInstance.setMethodBListener(bListener);
        pluginInstance.setMethodCListener(cListener);

        Map<ExtensionId, PluginInstance<? extends PluginInstanceAPI>> plugins = new HashMap<>();
        plugins.put(new ExtensionId(DEFAULT_ID), pluginInstance);
        operationsDataProcessor.setTransportContext(new TransportContext(Collections.unmodifiableMap(plugins), null, null, null,
                null, null, null, null, null, null));

        ByteBuffer bb = ByteBuffer.wrap(serializeMessages(getTestMessages()));

        List<ExtensionSync> extensionSyncs = new ArrayList<>();
        extensionSyncs.add(new ExtensionSync(DEFAULT_ID, bb));

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        response.setExtensionSyncResponses(extensionSyncs);

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        operationsDataProcessor.processResponse(converter.toByteArray(response));

        Mockito.verify(aListener, Mockito.times(1)).onEvent(Mockito.any(ClassC.class));
        Mockito.verify(bListener, Mockito.times(1)).onEvent(Mockito.any(ClassC.class));
        Mockito.verify(cListener, Mockito.times(1)).onEvent();
    }

    private List<Message> getTestMessages() throws IOException {
        List <Message> msgs = new ArrayList<>();
        msgs.add(new EntityMessage(UUID.randomUUID(), avroConverterA.toByteArray(new ClassA()), METHOD_1_ID));
        msgs.add(new EntityMessage(UUID.randomUUID(), avroConverterC.toByteArray(new ClassC()), METHOD_2_ID));
        msgs.add(new VoidMessage(UUID.randomUUID(), METHOD_3_ID));

        return msgs;
    }

    private byte[] serializeMessages(List<Message> msgs) {
        int length = 0;
        for (Message msg : msgs) {
            length += msg.getSize();
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[length + MESSAGE_COUNT_SIZE]);
        bb.putInt(msgs.size());
        for (Message msg : msgs) {
            msg.pushTo(bb);
        }
        return bb.array();
    }

    private void implementAsDirectExecutor(ExecutorService executor) {
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation)
                    throws Exception {
                Object[] args = invocation.getArguments();
                Runnable runnable = (Runnable) args[0];
                runnable.run();
                return null;
            }
        }).when(executor).execute(Mockito.any(Runnable.class));
    }

}
