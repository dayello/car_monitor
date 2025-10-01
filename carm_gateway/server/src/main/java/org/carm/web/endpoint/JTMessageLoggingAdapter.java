package org.carm.web.endpoint;

import io.github.yezhihao.netmc.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;
import org.carm.commons.spring.SSEService;
import org.carm.commons.util.JsonUtils;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.codec.JTMessageDecoder;
import org.carm.protocol.codec.JTMessageEncoder;
import org.carm.protocol.commons.MessageId;

/**
 * 增强版消息适配器，记录所有消息包括解码失败的消息
 */
@Slf4j
public class JTMessageLoggingAdapter extends JTMessagePushAdapter {

    public JTMessageLoggingAdapter(JTMessageEncoder messageEncoder, JTMessageDecoder messageDecoder, SSEService sseService) {
        super(messageEncoder, messageDecoder, sseService);
    }

    @Override
    public JTMessage decode(ByteBuf input, Session session) {
        // 记录原始消息
        String hexData = ByteBufUtil.hexDump(input, 0, input.writerIndex());
        log.info("{}\n<<<<<-原始消息接收,hex[{}]", session, hexData);
        
        // 通过SSE推送原始消息
        if (session != null && session.getClientId() != null) {
            String rawData = "原始消息:" + hexData;
            getSseService().send(session.getClientId(), rawData);
        }
        
        try {
            // 调用父类的解码方法
            JTMessage message = super.decode(input, session);
            
            if (message == null) {
                // 解码失败，记录详细信息
                log.warn("{}\n<<<<<-解码失败,无法解析消息,hex[{}]", session, hexData);
                
                // 通过SSE推送解码失败信息
                if (session != null && session.getClientId() != null) {
                    String failData = "解码失败:" + hexData;
                    getSseService().send(session.getClientId(), failData);
                }
            }
            
            return message;
        } catch (Exception e) {
            // 捕获解码异常
            log.error("{}\n<<<<<-解码异常,hex[{}],错误:{}", session, hexData, e.getMessage(), e);
            
            // 通过SSE推送异常信息
            if (session != null && session.getClientId() != null) {
                String errorData = "解码异常:" + hexData + ",错误:" + e.getMessage();
                getSseService().send(session.getClientId(), errorData);
            }
            
            return null;
        }
    }

    @Override
    public void decodeLog(Session session, JTMessage message, ByteBuf input) {
        // 调用父类方法进行正常的解码日志记录
        super.decodeLog(session, message, input);
        
        // 额外记录解码成功的详细信息
        if (message != null) {
            log.info("{}\n<<<<<-解码成功,消息ID:{},消息类型:{}", 
                    session, 
                    String.format("0x%04X", message.getMessageId()),
                    MessageId.getName(message.getMessageId()));
        }
    }

    @Override
    public void encodeLog(Session session, JTMessage message, ByteBuf output) {
        // 调用父类方法进行正常的编码日志记录
        super.encodeLog(session, message, output);
        
        // 额外记录编码详细信息
        log.info("{}\n>>>>>-编码完成,消息ID:{},消息类型:{}", 
                session, 
                String.format("0x%04X", message.getMessageId()),
                MessageId.getName(message.getMessageId()));
    }
    
    // 获取SSEService的辅助方法
    private SSEService getSseService() {
        try {
            // 通过反射获取父类的sseService字段
            java.lang.reflect.Field field = JTMessagePushAdapter.class.getDeclaredField("sseService");
            field.setAccessible(true);
            return (SSEService) field.get(this);
        } catch (Exception e) {
            log.warn("无法获取SSEService", e);
            return null;
        }
    }
}