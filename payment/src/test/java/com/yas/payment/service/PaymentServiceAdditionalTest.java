package com.yas.payment.service;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.repository.PaymentRepository;
import com.yas.payment.service.provider.handler.PaymentHandler;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * THÊM MỚI vào PaymentServiceTest.java
 * Không xoá code cũ.
 */
class PaymentServiceAdditionalTest {

    @Test
    void initPayment_WhenProviderNotFound_ShouldThrowException() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderService orderService = mock(OrderService.class);

        PaymentHandler handler = mock(PaymentHandler.class);
        when(handler.getProviderId()).thenReturn(PaymentMethod.PAYPAL.name());

        PaymentService paymentService =
                new PaymentService(paymentRepository, orderService, List.of(handler));

        paymentService.initializeProviders();

        InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                .paymentMethod("COD")
                .checkoutId("CHK001")
                .totalPrice(BigDecimal.TEN)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.initPayment(request));
    }

    @Test
    void capturePayment_WhenProviderNotFound_ShouldThrowException() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderService orderService = mock(OrderService.class);

        PaymentHandler handler = mock(PaymentHandler.class);
        when(handler.getProviderId()).thenReturn(PaymentMethod.PAYPAL.name());

        PaymentService paymentService =
                new PaymentService(paymentRepository, orderService, List.of(handler));

        paymentService.initializeProviders();

        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                .paymentMethod("COD")
                .token("TOKEN")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.capturePayment(request));
    }

    @Test
    void capturePayment_WhenFailureMessageExists_ShouldMapResponseCorrectly() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderService orderService = mock(OrderService.class);
        PaymentHandler handler = mock(PaymentHandler.class);

        when(handler.getProviderId()).thenReturn(PaymentMethod.PAYPAL.name());

        PaymentService paymentService =
                new PaymentService(paymentRepository, orderService, List.of(handler));

        paymentService.initializeProviders();

        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                .paymentMethod("PAYPAL")
                .token("TOKEN")
                .build();

        CapturedPayment captured = CapturedPayment.builder()
                .checkoutId("CHK001")
                .amount(BigDecimal.valueOf(100))
                .paymentFee(BigDecimal.ONE)
                .gatewayTransactionId("TXN001")
                .paymentMethod(PaymentMethod.PAYPAL)
                .paymentStatus(PaymentStatus.CANCELLED)
                .failureMessage("FAILED")
                .build();

        when(handler.capturePayment(request)).thenReturn(captured);
        when(orderService.updateCheckoutStatus(captured)).thenReturn(5L);

        when(paymentRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArgument(0));

        paymentService.capturePayment(request);
    }
}