package com.ebanking.auth_service.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public RSAKey rsaKey(
            @Value("${app.jwt.keystore-path}") String path,
            @Value("${app.jwt.keystore-password}") String password,
            @Value("${app.jwt.key-alias}") String alias) throws Exception {

        KeyStore store = KeyStore.getInstance("PKCS12");

        try (InputStream input = Files.newInputStream(Path.of(path))) {
            store.load(input, password.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) store.getKey(
                alias, password.toCharArray());

        X509Certificate certificate = (X509Certificate) store.getCertificate(alias);

        if (privateKey == null || certificate == null) {
            throw new IllegalStateException(
                    "JWT signing key not found");
        }

        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(alias)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        var jwkSource = new ImmutableJWKSet<SecurityContext>(
                new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }
}