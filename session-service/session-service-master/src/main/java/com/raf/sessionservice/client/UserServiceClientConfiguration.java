package com.raf.reservationservicevezbe.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.util.Collections;

@Configuration
@EnableRetry
public class UserServiceClientConfiguration {

    @Bean
    public RestTemplate userServiceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8080/api"));

        restTemplate.setInterceptors(Collections.singletonList(new TokenInterceptor()));
        return restTemplate;
    }

    private class TokenInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
                                            ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
            HttpHeaders headers = httpRequest.getHeaders();

            // TODO: OVO ZAMENI SA SVEZIM ADMIN TOKENOM KAD POKRENES USER SERVIS
            // Token mora imati ROLE_ADMIN jer neki endpointi (poput unosa statistike) traže admin privilegije ili internu proveru
            String adminToken = "Bearer TVOJ_NOVI_VALIDAN_TOKEN_OVDE";

            headers.add("Authorization", adminToken);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }
    }
}