package com.sharedexpenses.shared.security;

public interface CaptchaVerifier {

    boolean verify(String token, String clientIp);
}
