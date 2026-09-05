package com.medprep.security;

import org.springframework.core.convert.converter.Converter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;

public class JwtRoleConverter
        implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {

        String role = jwt.getClaimAsString("role");

        if(role == null || role.trim().isEmpty()) {

            return new JwtAuthenticationToken(
                    jwt,
                    Collections.emptyList()
            );
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + role.toUpperCase()
                );

        return new JwtAuthenticationToken(
                jwt,
                Collections.singletonList(authority)
        );
    }
}