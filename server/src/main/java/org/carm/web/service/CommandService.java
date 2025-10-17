package org.carm.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carm.commons.model.R;
import org.carm.protocol.commons.JT808;
import org.carm.protocol.t808.T0001;
import org.carm.protocol.t808.T8300;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {

    private final MessageService messageService;

    public Mono<R<T0001>> sendText(String text) {
        T8300 request = new T8300();
        request.setClientId("138001380000");      // target device
        request.setMessageId(JT808.文本信息下发);     // 0x8300
        request.setContent(text);
        request.setDriverId(0);                        // flags per spec

        return messageService.requestR(request, T0001.class);
    }
}