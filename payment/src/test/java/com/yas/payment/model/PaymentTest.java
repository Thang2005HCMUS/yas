package com.yas.payment.model;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/model/PaymentTest.java
 */
class PaymentTest {

    @Test
    void gettersAndSetters_ShouldWork() {
        Payment payment = new Payment();

        payment.setId(1L);
        payment.setOrderId(2L);
        payment.setCheckoutId("CHK001");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentFee(BigDecimal.valueOf(5));
        payment.setPaymentMethod(PaymentMethod.PAYPAL);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setGatewayTransactionId("TXN001");
        payment.setFailureMessage("NONE");
        payment.setPaymentProviderCheckoutId("PP001");

        assertEquals(1L, payment.getId());
        assertEquals(2L, payment.getOrderId());
        assertEquals("CHK001", payment.getCheckoutId());
        assertEquals(BigDecimal.valueOf(100), payment.getAmount());
        assertEquals(BigDecimal.valueOf(5), payment.getPaymentFee());
        assertEquals(PaymentMethod.PAYPAL, payment.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertEquals("TXN001", payment.getGatewayTransactionId());
        assertEquals("NONE", payment.getFailureMessage());
        assertEquals("PP001", payment.getPaymentProviderCheckoutId());
    }
}