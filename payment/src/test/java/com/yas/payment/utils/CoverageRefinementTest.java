package com.yas.payment.utils;

import com.yas.payment.viewmodel.ErrorVm;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageRefinementTest {
    @Test
    void testErrorVm_Constructor() {
        ErrorVm error = new ErrorVm("404", "Not Found", "Detail");
        assertThat(error.statusCode()).isEqualTo("404");
        assertThat(error.fieldErrors()).isEmpty(); // Phủ constructor phụ
    }

    @Test
    void testConstants_ErrorCode() {
        // Mẹo phủ constructor private của Utility Class
        assertThat(Constants.ErrorCode.PAYMENT_PROVIDER_NOT_FOUND).isEqualTo("PAYMENT_PROVIDER_NOT_FOUND");
    }
}