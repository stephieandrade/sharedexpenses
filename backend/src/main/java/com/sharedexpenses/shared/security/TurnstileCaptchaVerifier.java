package com.sharedexpenses.shared.security;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class TurnstileCaptchaVerifier implements CaptchaVerifier {

    private record VerifyResponse(boolean success) {
    }

    private final RestClient restClient;
    private final CaptchaProperties properties;

    public TurnstileCaptchaVerifier(RestClient.Builder restClientBuilder, CaptchaProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.properties = properties;
    }

    @Override
    public boolean verify(String token, String clientIp) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", properties.secret());
        form.add("response", token);
        form.add("remoteip", clientIp);

        VerifyResponse response = restClient.post()
                .uri(properties.verifyUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(VerifyResponse.class);
        return response != null && response.success();
    }
}
