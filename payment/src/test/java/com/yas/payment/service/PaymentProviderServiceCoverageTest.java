package com.yas.payment.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.payment.repository.PaymentProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProviderServiceCoverageTest {
    @Mock
    private PaymentProviderRepository repository;

    @InjectMocks
    private PaymentProviderService service;

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());
        
        // Đoạn này phủ nhánh orElseThrow trong findByIdOrElseThrow
        assertThrows(NotFoundException.class, () -> {
            service.getAdditionalSettingsByPaymentProviderId("invalid");
        });
    }
}