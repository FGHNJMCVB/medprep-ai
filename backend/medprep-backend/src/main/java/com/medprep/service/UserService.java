package com.medprep.service;

import com.medprep.dto.UserResponse;
import com.medprep.entity.User;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserId(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                )
                .getId();
    }
}