package com.yas.payment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/utils/MessagesUtilsTest.java
 */
class MessagesUtilsTest {

    @Test
    void getMessage_WhenCodeNotFound_ShouldReturnCodeItself() {
        String result = MessagesUtils.getMessage("UNKNOWN_ERROR_CODE");

        assertEquals("UNKNOWN_ERROR_CODE", result);
    }

    @Test
    void getMessage_WhenFormatArgumentsProvided_ShouldStillReturnValue() {
        String result = MessagesUtils.getMessage("UNKNOWN_{}", "123");

        assertNotNull(result);
    }
}