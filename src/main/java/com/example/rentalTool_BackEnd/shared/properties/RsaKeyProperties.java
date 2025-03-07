package com.example.rentalTool_BackEnd.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) {
}
