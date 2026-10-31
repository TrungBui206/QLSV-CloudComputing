package hd.quanlysinhvien_hibernatedao.security.authorization;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

abstract class AuthorizationTestBase {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.jwt.issuer}")
    private String issuer;

    protected MockMvc mockMvc;

    @BeforeEach
    void initializeMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    protected String bearerToken(Role role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(
                        role.name().toLowerCase(Locale.ROOT)
                )
                .claim(
                        "roles",
                        List.of(role.name())
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        header,
                        claims
                )
        ).getTokenValue();

        return "Bearer " + token;
    }
}