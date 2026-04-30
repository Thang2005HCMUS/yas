package com.yas.payment.service.provider.handler;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.*;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaypalHandlerTest {
    private PaypalService paypalService;
    private PaymentProviderService providerService;
    private PaypalHandler handler;

    @BeforeEach
    void setUp() {
        paypalService = mock(PaypalService.class);
        providerService = mock(PaymentProviderService.class);
        handler = new PaypalHandler(providerService, paypalService);
    }

    @Test
    void initPayment_shouldCallServiceAndMapResult() {
        InitPaymentRequestVm request = new InitPaymentRequestVm("PAYPAL", BigDecimal.TEN, "CHK1");
        
        when(providerService.getAdditionalSettingsByPaymentProviderId(any())).thenReturn("{}");
        when(paypalService.createPayment(any())).thenReturn(
            new PaypalCreatePaymentResponse("CREATED", "PAY-ID", "http://redirect")
        );

        var result = handler.initPayment(request);

        assertThat(result.getPaymentId()).isEqualTo("PAY-ID");
        verify(paypalService).createPayment(any());
    }

    @Test
    void getProviderId_shouldReturnPaypal() {
        assertThat(handler.getProviderId()).isEqualTo(PaymentMethod.PAYPAL.name());
    }
}