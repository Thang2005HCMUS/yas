// src/test/java/com/yas/payment/model/CapturedPaymentTest.java
package com.yas.payment.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CapturedPaymentTest {

    @Test
    void builder_shouldPopulateAllFields() {
        CapturedPayment payment = CapturedPayment.builder()
                .orderId(10L)
                .checkoutId("checkout-1")
                .amount(new BigDecimal("99.99"))
                .paymentFee(new BigDecimal("1.50"))
                .gatewayTransactionId("txn-001")
                .paymentMethod(PaymentMethod.PAYPAL)
                .paymentStatus(PaymentStatus.COMPLETED)
                .failureMessage(null)
                .build();

        assertThat(payment.getOrderId()).isEqualTo(10L);
        assertThat(payment.getCheckoutId()).isEqualTo("checkout-1");
        assertThat(payment.getAmount()).isEqualByComparingTo("99.99");
        assertThat(payment.getPaymentFee()).isEqualByComparingTo("1.50");
        assertThat(payment.getGatewayTransactionId()).isEqualTo("txn-001");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getFailureMessage()).isNull();
    }

    @Test
    void setters_shouldUpdateFields() {
        CapturedPayment payment = new CapturedPayment(
                1L,
                "old-checkout",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                "old-txn",
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                "old-error"
        );

        payment.setOrderId(2L);
        payment.setCheckoutId("new-checkout");
        payment.setAmount(new BigDecimal("50"));
        payment.setPaymentFee(new BigDecimal("2"));
        payment.setGatewayTransactionId("new-txn");
        payment.setPaymentMethod(PaymentMethod.BANKING);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setFailureMessage(null);

        assertThat(payment.getOrderId()).isEqualTo(2L);
        assertThat(payment.getCheckoutId()).isEqualTo("new-checkout");
        assertThat(payment.getAmount()).isEqualByComparingTo("50");
        assertThat(payment.getPaymentFee()).isEqualByComparingTo("2");
        assertThat(payment.getGatewayTransactionId()).isEqualTo("new-txn");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.BANKING);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getFailureMessage()).isNull();
    }
}