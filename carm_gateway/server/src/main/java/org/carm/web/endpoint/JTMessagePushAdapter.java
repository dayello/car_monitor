package org.carm.web.endpoint;

import io.github.yezhihao.netmc.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;
import org.carm.commons.spring.SSEService;
import org.carm.commons.util.JsonUtils;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.codec.JTMessageAdapter;
import org.carm.protocol.codec.JTMessageDecoder;
import org.carm.protocol.codec.JTMessageEncoder;
import org.carm.protocol.commons.JT808;
import org.carm.protocol.commons.MessageId;

import java.util.HashSet;

@Slf4j
public class JTMessagePushAdapter extends JTMessageAdapter {

    private final SSEService sseService;
    private static final HashSet<Integer> ignoreMsgs = new HashSet<>();

    static {
        ignoreMsgs.add(JT808.平台通用应答);
        ignoreMsgs.add(JT808.定位数据批量上传);
    }

    public JTMessagePushAdapter(JTMessageEncoder messageEncoder, JTMessageDecoder messageDecoder, SSEService sseService) {
        super(messageEncoder, messageDecoder);
        this.sseService = sseService;
    }

    @Override
    public void encodeLog(Session session, JTMessage message, ByteBuf output) {
        int messageId = message.getMessageId();
        String data = MessageId.getName(messageId) + JsonUtils.toJson(message) + ",hex:" + ByteBufUtil.hexDump(output, 0, output.writerIndex());
        sseService.send(message.getClientId(), data);
        if ((!ignoreMsgs.contains(messageId)))
            log.info("{}\n>>>>>-{}", session, data);
    }

    @Override
    public void decodeLog(Session session, JTMessage message, ByteBuf input) {
        if (message != null) {
            int messageId = message.getMessageId();
            String data = MessageId.getName(messageId) + JsonUtils.toJson(message) + ",hex:" + ByteBufUtil.hexDump(input, 0, input.writerIndex());
            sseService.send(message.getClientId(), data);
            if (!ignoreMsgs.contains(messageId))
                log.info("{}\n<<<<<-{}", session, data);

            if (!message.isVerified())
                log.error("<<<<<校验码错误session={},payload={}", session, data);
        }
    }

    public static void clearMessage() {
        synchronized (ignoreMsgs) {
            ignoreMsgs.clear();
        }
    }

    public static void addMessage(int messageId) {
        if (!ignoreMsgs.contains(messageId)) {
            synchronized (ignoreMsgs) {
                ignoreMsgs.add(messageId);
            }
        }
    }

    public static void removeMessage(int messageId) {
        if (ignoreMsgs.contains(messageId)) {
            synchronized (ignoreMsgs) {
                ignoreMsgs.remove(messageId);
            }
        }
    }
}