package org.carm.client.netty;

import io.github.yezhihao.netmc.core.model.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@Slf4j
@ChannelHandler.Sharable
public class TCPClientHandler extends ChannelInboundHandlerAdapter {

    private HandlerMapping handlerMapping;

    public TCPClientHandler(HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof Message))
            return;

        Message request = (Message) msg;
        Channel channel = ctx.channel();

        try {
            Handler handler = handlerMapping.getHandler(request.getMessageId());
            if (handler != null) {
                Message response = handler.invoke(request);

                if (response != null) {
                    channel.writeAndFlush(response);
                }
            }
        } catch (Exception e) {
            log.warn(String.valueOf(request), e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info(">>>>>连接到服务端{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("<<<<<断开连接{}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        log.error("<<<<<发生异常", e);
    }
}