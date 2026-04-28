package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_whenKeyExists_thenReturnFormattedMessage() {
        String message = MessagesUtils.getMessage("PRODUCT_NOT_FOUND", 1L);

        assertEquals("Product 1 is not found", message);
    }

    @Test
    void getMessage_whenKeyMissing_thenReturnErrorCode() {
        String message = MessagesUtils.getMessage("UNKNOWN_MESSAGE_CODE");

        assertEquals("UNKNOWN_MESSAGE_CODE", message);
    }
}
