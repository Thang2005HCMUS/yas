package com.yas.payment.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * THÊM MỚI file:
 * src/test/java/com/yas/payment/config/DatabaseAutoConfigTest.java
 */
class DatabaseAutoConfigTest {

    @Test
    void auditorAware_WhenNoAuthentication_ShouldReturnEmptyString() {
        DatabaseAutoConfig config = new DatabaseAutoConfig();

        SecurityContextHolder.clearContext();

        Optional<String> result = config.auditorAware().getCurrentAuditor();

        assertTrue(result.isPresent());
        assertEquals("", result.get());
    }

    @Test
    void auditorAware_WhenAuthenticationExists_ShouldReturnUsername() {
        DatabaseAutoConfig config = new DatabaseAutoConfig();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("tester");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> result = config.auditorAware().getCurrentAuditor();

        assertTrue(result.isPresent());
        assertEquals("tester", result.get());

        SecurityContextHolder.clearContext();
    }
}