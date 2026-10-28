package com.aifudi_clean_arch.backend.infra.security;

import com.aifudi_clean_arch.backend.core.exception.InvalidCredentialsException;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BasicHeaderAuthenticator {
    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;

    public UUID authenticate(String authHeader) {
        if (authHeader == null
                || !authHeader.startsWith("Basic ")) {
            throw new InvalidCredentialsException();
        }

        String credentials;
        try {
            byte[] decoded = Base64.getDecoder().decode(authHeader.substring(6).trim());
            credentials = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCredentialsException();
        }

        String[] values = credentials.split(":", 2);
        if (values.length != 2 || values[0].isBlank() || values[1].isEmpty()) {
            throw new InvalidCredentialsException();
        }

        var user = userGateway.getUserByLogin(values[0])
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(values[1], user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }
        return user.getId();
    }
}
