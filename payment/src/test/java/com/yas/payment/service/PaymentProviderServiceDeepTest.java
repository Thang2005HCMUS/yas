package com.yas.payment.service;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.repository.PaymentProviderRepository;
import com.yas.payment.viewmodel.paymentprovider.MediaVm;
import com.yas.payment.mapper.PaymentProviderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentProviderServiceDeepTest {

    @Test
    void getEnabledPaymentProviders_shouldCoverMapperAndMediaMap() {
        PaymentProviderRepository repo = mock(PaymentProviderRepository.class);
        MediaService mediaService = mock(MediaService.class);
        PaymentProviderMapper mapper = mock(PaymentProviderMapper.class);
        
        PaymentProviderService service = new PaymentProviderService(
                mediaService, mapper, null, null, repo
        );

        PaymentProvider p1 = new PaymentProvider();
        p1.setMediaId(1L);
        
        when(repo.findByEnabledTrue(any())).thenReturn(List.of(p1));
        when(mediaService.getMediaVmMap(any())).thenReturn(Map.of(1L, MediaVm.builder().url("url1").build()));
        when(mapper.toVm(any())).thenReturn(new com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm(
                "ID", "Name", "Url", 1, 1L, null));

        var result = service.getEnabledPaymentProviders(Pageable.unpaged());

        // Phủ logic gán iconUrl từ mediaVmMap[cite: 3]
        assertThat(result.get(0).getIconUrl()).isEqualTo("url1");
    }
}