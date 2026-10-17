
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.service.JwtTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenServiceImpl(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public AuthResponse generateToken(User user) {

        Instant now = Instant.now();

        Instant expiresAt = now.plus(
                expirationMinutes,
                ChronoUnit.MINUTES
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("roles", List.of(user.getRole().name()))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new AuthResponse(
                token,
                "Bearer",
                expirationMinutes * 60,
                UserResponse.from(user)
        );
    }
}
