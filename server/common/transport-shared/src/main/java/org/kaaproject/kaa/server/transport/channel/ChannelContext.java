package org.kaaproject.kaa.server.transport.channel;

public interface ChannelContext {

    void writeAndFlush(Object response);

    void fireExceptionCaught(Exception e);

    void write(Object object);

    void flush();

}
