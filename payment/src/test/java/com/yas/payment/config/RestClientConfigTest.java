package com.yas.payment.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/config/RestClientConfigTest.java
 */
class RestClientConfigTest {

    @Test
    void getRestClient_ShouldCreateBeanSuccessfully() {
        RestClientConfig config = new RestClientConfig();

        RestClient.Builder builder = RestClient.builder();

        RestClient client = config.getRestClient(builder);

        assertNotNull(client);
    }
}