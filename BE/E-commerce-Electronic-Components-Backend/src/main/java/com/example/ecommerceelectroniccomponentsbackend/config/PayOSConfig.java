package com.example.ecommerceelectroniccomponentsbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Data
@Configuration
@ConfigurationProperties(prefix = "payos")
public class PayOSConfig {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String returnUrl;
    private String cancelUrl;

    @Bean
    public PayOS payOS() {
        String cId = (clientId != null && !clientId.isBlank()) ? clientId : "dummy-client-id";
        String aKey = (apiKey != null && !apiKey.isBlank()) ? apiKey : "dummy-api-key";
        String csKey = (checksumKey != null && !checksumKey.isBlank()) ? checksumKey : "dummy-checksum-key";
        return new PayOS(cId, aKey, csKey);
    }
}
