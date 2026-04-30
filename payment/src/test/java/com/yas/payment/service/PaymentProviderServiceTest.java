package com.yas.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.payment.mapper.CreatePaymentProviderMapper;
import com.yas.payment.mapper.PaymentProviderMapper;
import com.yas.payment.mapper.UpdatePaymentProviderMapper;
import com.yas.payment.model.PaymentProvider;
import com.yas.payment.repository.PaymentProviderRepository;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.MediaVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Pageable;

class PaymentProviderServiceTest {

    public static final String[] IGNORED_FIELDS = {"version", "iconUrl"};

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private PaymentProviderService paymentProviderService;

    @Mock
    private PaymentProviderRepository paymentProviderRepository;

    @Spy
    private PaymentProviderMapper paymentProviderMapper = Mappers.getMapper(PaymentProviderMapper.class);
    @Spy
    private CreatePaymentProviderMapper createPaymentProviderMapper = Mappers.getMapper(CreatePaymentProviderMapper.class);
    @Spy
    private UpdatePaymentProviderMapper updatePaymentProviderMapper = Mappers.getMapper(UpdatePaymentProviderMapper.class);

    private PaymentProvider paymentProvider;
    private Pageable defaultPageable = Pageable.ofSize(10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentProvider = new PaymentProvider();
        paymentProvider.setId("providerId");
        paymentProvider.setAdditionalSettings("additional settings");
        paymentProvider.setEnabled(true);
        paymentProvider.setMediaId(100L);
    }

    @Test
    void createPaymentProvider_Success() {
        var randomVal = UUID.randomUUID().toString();
        CreatePaymentVm createReq = getCreatePaymentVm(randomVal);
        when(paymentProviderRepository.save(any())).thenReturn(getPaymentProvider(randomVal));
        var result = paymentProviderService.create(createReq);
        verify(paymentProviderRepository).save(any());
        assertThat(result.getId()).isEqualTo(randomVal);
    }

    @Test
    void updatePaymentProvider_NotFound_ThrowsException() {
        UpdatePaymentVm updateReq = new UpdatePaymentVm();
        updateReq.setId("non-existent");
        when(paymentProviderRepository.findById("non-existent")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> paymentProviderService.update(updateReq)); 
    }

    // --- NEW TEST CASES TO INCREASE COVERAGE ---

    @Test
    void getEnabledPaymentProviders_WithMediaMapping_Success() {
        // Given
        List<PaymentProvider> providers = List.of(paymentProvider);
        when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(providers);
        
        MediaVm mediaVm = MediaVm.builder().id(100L).url("http://image.url/icon.png").build();
        when(mediaService.getMediaVmMap(providers)).thenReturn(Map.of(100L, mediaVm)); 

        // When
        List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIconUrl()).isEqualTo("http://image.url/icon.png"); 
        verify(mediaService).getMediaVmMap(any());
    }

    @Test
    void getEnabledPaymentProviders_WhenNoProvider_ReturnsEmptyList() {
        when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(List.of()); 
        List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);
        assertThat(result).isEmpty(); 
    }

    @Test
    void getAdditionalSettings_WhenProviderExists_ReturnsSettings() {
        when(paymentProviderRepository.findById("providerId")).thenReturn(Optional.of(paymentProvider));
        String settings = paymentProviderService.getAdditionalSettingsByPaymentProviderId("providerId");
        assertThat(settings).isEqualTo("additional settings"); 
    }

    // --- HELPERS ---
    private static CreatePaymentVm getCreatePaymentVm(String val) {
        CreatePaymentVm vm = new CreatePaymentVm();
        vm.setId(val);
        vm.setName(val);
        vm.setConfigureUrl(val);
        return vm;
    }

    private static PaymentProvider getPaymentProvider(String val) {
        PaymentProvider p = new PaymentProvider();
        p.setId(val);
        p.setName(val);
        return p;
    }
}