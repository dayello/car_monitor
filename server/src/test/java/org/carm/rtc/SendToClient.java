package org.carm.rtc;

import lombok.RequiredArgsConstructor;
import org.carm.protocol.basics.JTMessage;
import org.carm.protocol.commons.JT808;
import org.carm.web.service.MessageService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SendToClient {
    private final MessageService messageService;

    public Mono<Void> notifyTerminal() {
        JTMessage req = new JTMessage();
        req.setClientId("138001380000");
        req.setMessageId(JT808.服务器向终端发起链路检测请求); // 0x8204
        return messageService.notify(req.getClientId(), req);
    }
}