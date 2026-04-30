// src/test/java/com/yas/payment/model/InitiatedPaymentTest.java
package com.yas.payment.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InitiatedPaymentTest {

    @Test
    void builder_shouldPopulateAllFields() {
        InitiatedPayment payment = InitiatedPayment.builder()
                .status("SUCCESS")
                .paymentId("PAY-123")
                .redirectUrl("https://redirect.url")
                .build();

        assertThat(payment.getStatus()).isEqualTo("SUCCESS");
        assertThat(payment.getPaymentId()).isEqualTo("PAY-123");
        assertThat(payment.getRedirectUrl()).isEqualTo("https://redirect.url");
    }

    @Test
    void setters_shouldUpdateFields() {
        InitiatedPayment payment = new InitiatedPayment(
                "PENDING",
                "OLD-ID",
                "old-url"
        );

        payment.setStatus("DONE");
        payment.setPaymentId("NEW-ID");
        payment.setRedirectUrl("new-url");

        assertThat(payment.getStatus()).isEqualTo("DONE");
        assertThat(payment.getPaymentId()).isEqualTo("NEW-ID");
        assertThat(payment.getRedirectUrl()).isEqualTo("new-url");
    }
}