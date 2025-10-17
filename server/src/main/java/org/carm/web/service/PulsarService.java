package org.carm.web.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.carm.protocol.basics.JTMessage;
import org.carm.web.config.PulsarProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(value = "jt-server.pulsar.enabled", havingValue = "true")
public class PulsarService {

    @Resource
    private PulsarProperties props;

    private PulsarClient client;
    private Producer<byte[]> upProducer;
    private Producer<byte[]> downProducer;

    @PostConstruct
    public void init() {
        try {
            client = PulsarClient.builder()
                    .serviceUrl(props.getServiceUrl())
                    .build();
            upProducer = client.newProducer()
                    .topic(props.getTopicUp())
                    .producerName(props.getProducerNamePrefix() + "-up")
                    .create();
            downProducer = client.newProducer()
                    .topic(props.getTopicDown())
                    .producerName(props.getProducerNamePrefix() + "-down")
                    .create();
            log.info("Pulsar initialized. serviceUrl={}, up={}, down={}", props.getServiceUrl(), props.getTopicUp(), props.getTopicDown());
        } catch (Exception e) {
            log.error("Failed to initialize Pulsar", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (upProducer != null) upProducer.close();
            if (downProducer != null) downProducer.close();
            if (client != null) client.close();
        } catch (Exception e) {
            log.warn("Error while closing Pulsar resources", e);
        }
    }

    public void publishUp(JTMessage message, String hex, String sessionInfo) {
        if (upProducer == null) return;
        try {
            byte[] payload = buildPayload("UP", message, hex, sessionInfo);
            upProducer.sendAsync(payload).exceptionally(ex -> {
                log.warn("Pulsar publishUp failed: {}", ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            log.warn("Pulsar publishUp error", e);
        }
    }

    public void publishDown(JTMessage message, String hex, String sessionInfo) {
        if (downProducer == null) return;
        try {
            byte[] payload = buildPayload("DOWN", message, hex, sessionInfo);
            downProducer.sendAsync(payload).exceptionally(ex -> {
                log.warn("Pulsar publishDown failed: {}", ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            log.warn("Pulsar publishDown error", e);
        }
    }

    private byte[] buildPayload(String direction, JTMessage message, String hex, String sessionInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", direction);
        map.put("clientId", message.getClientId());
        map.put("messageId", message.getMessageId());
        map.put("hex", hex);
        map.put("session", sessionInfo);
        String json = org.carm.commons.util.JsonUtils.toJson(map);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}