package org.carm.web.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息日志处理器
 * 用于记录所有接收到的消息，包括解码失败的消息
 */
@Slf4j
public class JTMessageLoggingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            // 记录原始消息的十六进制表示
            log.info("收到原始消息: {}", ByteBufUtil.hexDump(buf));
            
            // 重要：保持引用计数不变，确保消息可以继续传递给下一个处理器
            // 复制一份ByteBuf，以便不影响原始数据
            ByteBuf copied = buf.copy();
            try {
                // 将原始消息传递给下一个处理器
                ctx.fireChannelRead(msg);
            } finally {
                // 释放复制的ByteBuf
                ReferenceCountUtil.release(copied);
            }
        } else {
            // 如果不是ByteBuf类型，直接传递给下一个处理器
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("消息处理异常", cause);
        // 将异常传递给下一个处理器
        ctx.fireExceptionCaught(cause);
    }
}