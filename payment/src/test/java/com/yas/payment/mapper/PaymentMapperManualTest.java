package com.yas.payment.mapper;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperManualTest {
    private final CreatePaymentProviderMapper createMapper = Mappers.getMapper(CreatePaymentProviderMapper.class);
    private final PaymentProviderMapper providerMapper = Mappers.getMapper(PaymentProviderMapper.class);

    @Test
    void createMapper_toVm_shouldMapNull() {
        assertThat(createMapper.toVm(null)).isNull(); // Phủ nhánh if (provider == null)
    }

    @Test
    void createMapper_toModel_shouldSetIsNewTrue() {
        CreatePaymentVm vm = new CreatePaymentVm();
        vm.setId("test-id");
        PaymentProvider model = createMapper.toModel(vm);
        assertThat(model.isNew()).isTrue(); // Phủ @Mapping isNew = true
    }

    @Test
    void providerMapper_partialUpdate_shouldHandleNulls() {
        PaymentProvider target = new PaymentProvider();
        providerMapper.partialUpdate(target, null); // Phủ nhánh validation null trong Impl
        assertThat(target.getId()).isNull();
    }
}