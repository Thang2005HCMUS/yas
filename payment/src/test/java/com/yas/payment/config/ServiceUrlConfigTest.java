// src/test/java/com/yas/payment/config/ServiceUrlConfigTest.java
package com.yas.payment.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServiceUrlConfigTest {

    @Test
    void record_shouldReturnConfiguredValues() {
        ServiceUrlConfig config =
                new ServiceUrlConfig("http://order-service", "http://media-service");

        assertThat(config.order()).isEqualTo("http://order-service");
        assertThat(config.media()).isEqualTo("http://media-service");
    }

    @Test
    void equalHashCodeAndToString_shouldWork() {
        ServiceUrlConfig a =
                new ServiceUrlConfig("order-url", "media-url");
        ServiceUrlConfig b =
                new ServiceUrlConfig("order-url", "media-url");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).contains("order-url", "media-url");
    }
}