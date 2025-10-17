package org.carm.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jt-server.pulsar")
public class PulsarProperties {
    private boolean enabled = false;
    private String serviceUrl = "pulsar://localhost:6650";
    private String topicUp = "persistent://public/default/jt808-up";
    private String topicDown = "persistent://public/default/jt808-down";
    private String producerNamePrefix = "jt808-gateway";
}
