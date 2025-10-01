package org.carm.web.endpoint;

import io.github.yezhihao.netmc.session.Session;
import io.github.yezhihao.netmc.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.carm.commons.model.APIException;
import org.carm.commons.model.R;
import org.carm.protocol.basics.JTMessage;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@Slf4j
@Component
public class MessageManager {

    private static final Mono<Void> NEVER = Mono.never();
    private static final Mono OFFLINE_EXCEPTION = Mono.error(new APIException(4000, "离线的客户端（请检查设备是否注册或者鉴权）"));
    private static final Mono OFFLINE_RESULT = Mono.just(R.error("离线的客户端（请检查设备是否注册或者鉴权）").setCode(4000));
    private static final Mono SENDFAIL_RESULT = Mono.just(R.error("消息发送失败").setCode(4001));
    private static final Mono TIMEOUT_RESULT = Mono.just(R.error("消息发送成功,客户端响应超时（至于设备为什么不应答，请联系设备厂商）").setCode(4002));

    private SessionManager sessionManager;

    public MessageManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Mono<Void> notifyR(String sessionId, JTMessage request) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_EXCEPTION;

        return session.notify(request);
    }

    public Mono<Void> notify(String sessionId, JTMessage request) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return NEVER;

        return session.notify(request);
    }

    public <T> Mono<R<T>> requestR(String sessionId, JTMessage request, Class<T> responseClass) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_RESULT;

        return session.request(request, responseClass)
                .map(message -> R.success(message))
                .timeout(Duration.ofSeconds(10), TIMEOUT_RESULT)
                .onErrorResume(e -> {
                    log.warn("消息发送失败", e);
                    return SENDFAIL_RESULT;
                });
    }

    public <T> Mono<R<T>> requestR(JTMessage request, Class<T> responseClass) {
        Session session = sessionManager.get(request.getClientId());
        if (session == null)
            return OFFLINE_RESULT;

        return session.request(request, responseClass)
                .map(message -> R.success(message))
                .timeout(Duration.ofSeconds(10), TIMEOUT_RESULT)
                .onErrorResume(e -> {
                    log.warn("消息发送失败", e);
                    return SENDFAIL_RESULT;
                });
    }

    public <T> Mono<T> request(String sessionId, JTMessage request, Class<T> responseClass, long timeout) {
        return request(sessionId, request, responseClass).timeout(Duration.ofMillis(timeout));
    }

    public <T> Mono<T> request(String sessionId, JTMessage request, Class<T> responseClass) {
        Session session = sessionManager.get(sessionId);
        if (session == null)
            return OFFLINE_EXCEPTION;

        return session.request(request, responseClass);
    }
}