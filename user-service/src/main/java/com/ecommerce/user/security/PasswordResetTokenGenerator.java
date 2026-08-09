package com.ecommerce.user.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_MINUTES = 30;

    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedResetToken generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String tokenHash = hash(rawToken);

        Instant expiresAt = Instant.now()
                .plus(EXPIRY_MINUTES, ChronoUnit.MINUTES);

        return new GeneratedResetToken(
                rawToken,
                tokenHash,
                expiresAt
        );
    }

    public String hash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception);
        }
    }

    public record GeneratedResetToken(
            String rawToken,
            String tokenHash,
            Instant expiresAt
    ) {
    }
}