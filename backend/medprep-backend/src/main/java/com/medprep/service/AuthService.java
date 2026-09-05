package com.medprep.service;

import com.medprep.dto.AuthResponse;
import com.medprep.dto.LoginRequest;
import com.medprep.dto.RegisterRequest;
import com.medprep.entity.Role;
import com.medprep.entity.User;
import com.medprep.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtEncoder jwtEncoder,
                       Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = Long.parseLong(
                environment.getProperty("app.jwt.expiration-ms", "86400000")
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);

        User saved = userRepository.save(user);

        String token = generateToken(saved);

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String token = generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    private String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMs, ChronoUnit.MILLIS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("medprep-backend")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();
    }
}
