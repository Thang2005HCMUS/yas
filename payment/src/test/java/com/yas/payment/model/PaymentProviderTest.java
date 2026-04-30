package com.yas.payment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/model/PaymentProviderTest.java
 */
class PaymentProviderTest {

    @Test
    void isNew_ShouldReturnTrue_WhenFlagIsTrue() {
        PaymentProvider provider = new PaymentProvider();
        provider.setNew(true);

        assertTrue(provider.isNew());
    }

    @Test
    void isNew_ShouldReturnFalse_WhenFlagIsFalse() {
        PaymentProvider provider = new PaymentProvider();
        provider.setNew(false);

        assertFalse(provider.isNew());
    }

    @Test
    void gettersAndSetters_ShouldWork() {
        PaymentProvider provider = new PaymentProvider();

        provider.setId("PAYPAL");
        provider.setEnabled(true);
        provider.setName("Paypal");
        provider.setConfigureUrl("/config");
        provider.setLandingViewComponentName("paypal-view");
        provider.setAdditionalSettings("{}");
        provider.setMediaId(10L);
        provider.setVersion(2);

        assertEquals("PAYPAL", provider.getId());
        assertTrue(provider.isEnabled());
        assertEquals("Paypal", provider.getName());
        assertEquals("/config", provider.getConfigureUrl());
        assertEquals("paypal-view", provider.getLandingViewComponentName());
        assertEquals("{}", provider.getAdditionalSettings());
        assertEquals(10L, provider.getMediaId());
        assertEquals(2, provider.getVersion());
    }
}