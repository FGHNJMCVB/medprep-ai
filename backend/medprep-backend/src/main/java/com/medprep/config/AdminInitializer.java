package com.medprep.config;

import com.medprep.entity.Role;
import com.medprep.entity.User;
import com.medprep.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String email = "admin@medprep.com";

            if(userRepository.findByEmail(email).isPresent()) {
                return;
            }

            User admin = new User();

            admin.setEmail(email);
            admin.setFirstName("MedPrep");
            admin.setLastName("Admin");

            admin.setPassword(
                    passwordEncoder.encode(
                            "Admin@12345"
                    )
            );

            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        };
    }
}