package com.yas.sampledata.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigCoverageTest {

    @Test
    void restClientConfig_ShouldReturnBean() {
        RestClientConfig config = new RestClientConfig();
        RestClient client = config.restClient();
        assertNotNull(client);
    }
}