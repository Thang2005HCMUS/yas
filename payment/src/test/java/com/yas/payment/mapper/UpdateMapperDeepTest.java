package com.yas.payment.mapper;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class UpdateMapperDeepTest {
    private final UpdatePaymentProviderMapper mapper = Mappers.getMapper(UpdatePaymentProviderMapper.class);

    @Test
    void partialUpdate_shouldCoverEveryFieldBranch() {
        PaymentProvider target = new PaymentProvider();
        UpdatePaymentVm vm = new UpdatePaymentVm();
        
        // Test trường hợp các field đều có giá trị để phủ nhánh "if (vm.get... != null)"
        vm.setName("New Name");
        vm.setEnabled(true);
        vm.setConfigureUrl("/new-url");
        vm.setLandingViewComponentName("comp");
        vm.setAdditionalSettings("{}");
        vm.setMediaId(100L);

        mapper.partialUpdate(target, vm);

        assertThat(target.getName()).isEqualTo("New Name");
        assertThat(target.getMediaId()).isEqualTo(100L);
        assertThat(target.isEnabled()).isTrue();
    }

    @Test
    void toVmResponse_shouldHandleNullProvider() {
        // Phủ nhánh kiểm tra null đầu vào của phương thức toVmResponse
        assertThat(mapper.toVmResponse(null)).isNull();
    }
}