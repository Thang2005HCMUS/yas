package com.yas.search;

import com.yas.search.config.ServiceUrlConfig;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.boot.CommandLineRunner;
@EnableConfigurationProperties(ServiceUrlConfig.class)
@SpringBootApplication(scanBasePackages = {"com.yas.search", "com.yas.commonlibrary"})
@Configuration
public class ElasticsearchApplication {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchApplication.class, args);
    }
    @Bean
    public CommandLineRunner debugElasticsearchConfig(
            @Value("${spring.elasticsearch.uris:Not Found}") String uris,
            @Value("${spring.elasticsearch.username:Not Found}") String username,
            @Value("${spring.elasticsearch.password:Not Found}") String password,
            // Dự phòng trường hợp ứng dụng dùng biến custom không có chữ spring:
            @Value("${elasticsearch.url:Not Found}") String customUrl,
            @Value("${elasticsearch.username:Not Found}") String customUsername,
            @Value("${elasticsearch.password:Not Found}") String customPassword) {
        
        return args -> {
            log.info("====================================================");
            log.info("     DIAGNOSING ELASTICSEARCH CONFIGURATION         ");
            log.info("====================================================");
            log.info("[Spring Config] URIs: {}", uris);
            log.info("[Spring Config] Username: {}", username);
            log.info("[Spring Config] Password: {}", password);
            log.info("----------------------------------------------------");
            log.info("[Custom Config] URL: {}", customUrl);
            log.info("[Custom Config] Username: {}", customUsername);
            log.info("[Custom Config] Password: {}", customPassword);
            log.info("====================================================");
        };
    }
}