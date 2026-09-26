package com.medprep.config;

import com.medprep.entity.Role;
import com.medprep.entity.User;
import com.medprep.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:}") String email,
            @Value("${ADMIN_PASSWORD:}") String password) {

        return args -> {

            if (email.isBlank() && password.isBlank()) {
                return;
            }

            if (email.isBlank() || password.length() < 12) {
                throw new IllegalStateException(
                        "ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters are required together"
                );
            }

            User existing = userRepository.findByEmail(email).orElse(null);

            if (existing != null) {
                if (existing.getRole() != Role.ADMIN) {
                    throw new IllegalStateException("ADMIN_EMAIL belongs to a non-admin account");
                }
                existing.setPassword(passwordEncoder.encode(password));
                userRepository.save(existing);
                return;
            }

            User admin = new User();

            admin.setEmail(email);
            admin.setFirstName("MedPrep");
            admin.setLastName("Admin");

            admin.setPassword(
                    passwordEncoder.encode(
                            password
                    )
            );

            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        };
    }
}
