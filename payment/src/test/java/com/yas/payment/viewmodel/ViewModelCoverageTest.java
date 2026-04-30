package com.yas.payment.viewmodel;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class ViewModelCoverageTest {

    @Test
    void capturePaymentResponseVm_FullCoverage() {
        CapturePaymentResponseVm vm1 = CapturePaymentResponseVm.builder()
                .orderId(1L).checkoutId("C1").build();
        CapturePaymentResponseVm vm2 = CapturePaymentResponseVm.builder()
                .orderId(1L).checkoutId("C1").build();

        // Kích hoạt equals, hashCode và toString
        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).contains("orderId=1");
    }

    @Test
    void paymentOrderStatusVm_FullCoverage() {
        PaymentOrderStatusVm vm = new PaymentOrderStatusVm(1L, "S1", 2L, "P1");
        assertThat(vm.orderStatus()).isEqualTo("S1");
        assertThat(vm.paymentStatus()).isEqualTo("P1");
    }
}