package com.example.ecommerceelectroniccomponentsbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.Data;

import java.net.http.HttpClient;

@Configuration
@ConfigurationProperties(prefix = "smart-locker")
@Data
public class SmartLockerConfig {

    private String apiUrl = "http://localhost:8000";

    @Bean
    public RestClient smartLockerRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
