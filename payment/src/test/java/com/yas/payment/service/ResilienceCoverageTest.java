package com.yas.payment.service;

import com.yas.payment.config.ServiceUrlConfig;
import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.viewmodel.PaymentOrderStatusVm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ResilienceCoverageTest {

    @Test
    void orderService_fallbackMethods_shouldThrowOriginalError() {
        OrderService service = new OrderService(mock(RestClient.class), mock(ServiceUrlConfig.class));
        Throwable cause = new RuntimeException("Circuit open");

        // Phủ handleLongFallback và handlePaymentOrderStatusFallback
        assertThrows(RuntimeException.class, () -> service.handleLongFallback(cause));
        assertThrows(RuntimeException.class, () -> service.handlePaymentOrderStatusFallback(cause));
    }

    @Test
    void mediaService_fallback_shouldReturnEmptyMap() {
        MediaService service = new MediaService(mock(RestClient.class), mock(ServiceUrlConfig.class));
        
        // Phủ phương thức fallbackGetMediaVmMap
        var result = service.getMediaVmMap(java.util.Collections.emptyList());
        assertTrue(result.isEmpty());
    }
}