package com.microservices.authservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    @Test
    void restTemplate_isBuiltWithCustomFactory() throws Exception {
        RestTemplate restTemplate = new RestTemplateConfig().restTemplate();

        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }
}
