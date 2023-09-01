package com.emmanuel.sarabrandserver.auth.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Logger;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service @Getter @Setter
public class JwtTokenService {
    private static final Logger log = Logger.getLogger(JwtTokenService.class.getName());

    private int tokenExpiry = 30; // minutes.
    private int boundToSendRefreshToken = 15; // minutes

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Generates a jwt token
     * @param authentication of type org.springframework.security.core
     * @return String
     * */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        String[] role = authentication.getAuthorities() //
                .stream() //
                .map(grantedAuthority -> StringUtils.substringAfter(grantedAuthority.getAuthority(), "ROLE_"))
                .toArray(String[]::new);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(tokenExpiry, MINUTES))
                .subject(authentication.getName())
                .claim("role", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /** Simply validates if token is expired or not */
    public boolean _isTokenNoneExpired(@NotNull final Cookie cookie) {
        try {
            this.jwtDecoder.decode(cookie.getValue());
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    /**
     * Returns true if token is within expiration bound
     * @param cookie of type jakarta.servlet.http.Cookie
     * @return boolean
     * */
    public boolean _refreshTokenNeeded(@NotNull final Cookie cookie) {
        try {
            Jwt jwt = this.jwtDecoder.decode(cookie.getValue()); // throws an error if jwt is not valid
            var expiresAt = jwt.getExpiresAt();
            var now = Instant.now();
            var bound = now.plus(boundToSendRefreshToken, MINUTES);
            return expiresAt.isAfter(now) && expiresAt.isBefore(bound);
        } catch (JwtException | NullPointerException e) {
            log.warning("JWT exception %s, %s".formatted(e.getMessage(), RefreshTokenFilter.class));
            return false;
        }
    }

    // Convert tokenExpiry to seconds
    public int maxAge() {
        return this.getTokenExpiry() * 60;
    }

    public String extractSubject(final Cookie cookie) {
        return this.jwtDecoder.decode(cookie.getValue()).getSubject();
    }

}
