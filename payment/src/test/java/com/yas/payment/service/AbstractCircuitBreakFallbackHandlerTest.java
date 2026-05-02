package com.yas.payment.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/service/AbstractCircuitBreakFallbackHandlerTest.java
 */
class AbstractCircuitBreakFallbackHandlerTest {

    static class TestHandler extends AbstractCircuitBreakFallbackHandler {

        void callBodiless(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        String callTyped(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }

    @Test
    void handleBodilessFallback_ShouldThrowOriginalException() {
        TestHandler handler = new TestHandler();

        assertThrows(RuntimeException.class,
                () -> handler.callBodiless(new RuntimeException("boom")));
    }

    @Test
    void handleTypedFallback_ShouldThrowOriginalException() {
        TestHandler handler = new TestHandler();

        assertThrows(IllegalStateException.class,
                () -> handler.callTyped(new IllegalStateException("failed")));
    }
}