package com.pado.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProps {
    private String secret;
    private long accessExpSeconds;

    public void setSecret(String secret) {
        this.secret = secret;
    }
    public void setAccessExpSeconds(long accessExpSeconds) {
        this.accessExpSeconds = accessExpSeconds;
    }

}
