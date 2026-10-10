package com.example.ecommerceelectroniccomponentsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class ECommerceElectronicComponentsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceElectronicComponentsBackendApplication.class, args);
    }

}
