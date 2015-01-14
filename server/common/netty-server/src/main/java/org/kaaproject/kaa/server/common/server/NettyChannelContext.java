package org.kaaproject.kaa.server.common.server;

import io.netty.channel.ChannelHandlerContext;

import org.kaaproject.kaa.server.transport.channel.ChannelContext;

public class NettyChannelContext implements ChannelContext{
    private final ChannelHandlerContext ctx;

    public NettyChannelContext(ChannelHandlerContext ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public void writeAndFlush(Object msg) {
        ctx.writeAndFlush(msg);
    }

    @Override
    public void fireExceptionCaught(Exception e) {
        ctx.fireExceptionCaught(e);
    }

    @Override
    public void write(Object msg) {
        ctx.write(msg);
    }

    @Override
    public void flush() {
        ctx.flush();
    }
}