package com.yas.payment.service;

import com.yas.payment.config.ServiceUrlConfig;
import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.MediaVm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * THÊM MỚI file test mới:
 * src/test/java/com/yas/payment/service/MediaServiceAdditionalTest.java
 */
class MediaServiceAdditionalTest {

    @Test
    void fallbackGetMediaVmMap_ShouldReturnEmptyMap() throws Exception {
        RestClient restClient = mock(RestClient.class);
        ServiceUrlConfig config = mock(ServiceUrlConfig.class);

        MediaService service = new MediaService(restClient, config);

        PaymentProvider provider = new PaymentProvider();
        provider.setMediaId(10L);

        Method method = MediaService.class.getDeclaredMethod(
                "fallbackGetMediaVmMap",
                List.class,
                Throwable.class
        );

        method.setAccessible(true);

        Map<Long, MediaVm> result =
                (Map<Long, MediaVm>) method.invoke(
                        service,
                        List.of(provider),
                        new RuntimeException("boom")
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void getMediasUrl_ShouldBuildCorrectUrl() throws Exception {
        RestClient restClient = mock(RestClient.class);
        ServiceUrlConfig config = mock(ServiceUrlConfig.class);

        Mockito.when(config.media()).thenReturn("http://media-service");

        MediaService service = new MediaService(restClient, config);

        Method method = MediaService.class.getDeclaredMethod(
                "getMediasUrl",
                Set.class
        );

        method.setAccessible(true);

        URI uri = (URI) method.invoke(service, Set.of(1L, 2L));

        assertTrue(uri.toString().contains("http://media-service"));
        assertTrue(uri.toString().contains("/medias"));
        assertTrue(uri.toString().contains("ids="));
    }

    @Test
    void getMediaIds_ShouldExtractIds() throws Exception {
        RestClient restClient = mock(RestClient.class);
        ServiceUrlConfig config = mock(ServiceUrlConfig.class);

        MediaService service = new MediaService(restClient, config);

        PaymentProvider p1 = new PaymentProvider();
        p1.setMediaId(1L);

        PaymentProvider p2 = new PaymentProvider();
        p2.setMediaId(2L);

        Method method = MediaService.class.getDeclaredMethod(
                "getMediaIds",
                List.class
        );

        method.setAccessible(true);

        Set<Long> result = (Set<Long>) method.invoke(service, List.of(p1, p2));

        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }
}