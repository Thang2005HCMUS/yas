package com.yas.payment.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


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
    @Test
    void getRestClient_shouldBuildClientWithJsonHeader() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient client = mock(RestClient.class);

        when(builder.defaultHeader(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE
        )).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        RestClientConfig config = new RestClientConfig();
        RestClient result = config.getRestClient(builder);

        assertThat(result).isSameAs(client);
        verify(builder).defaultHeader(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE
        );
        verify(builder).build();
    }
}