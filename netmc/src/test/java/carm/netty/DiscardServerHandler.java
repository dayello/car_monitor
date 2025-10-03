package carm.netty;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 处理服务端 channel.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // 默默地丢弃收到的数据
        ((ByteBuf) msg).release(); // (3)

//        ByteBuf in = (ByteBuf) msg;
        try {
//                // 服务端打印接收到的消息
//            while (in.isReadable()) { // (1)
//                System.out.print((char) in.readByte());
//                System.out.flush();
//            }

            // 返回原本信息给客户端
//            ctx.writeAndFlush(msg);
            // 返回原本信息给客户端--并关闭连接
//            ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
        } finally {
//            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
