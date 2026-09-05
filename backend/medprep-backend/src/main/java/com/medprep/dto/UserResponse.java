package com.medprep.dto;

import com.medprep.entity.Role;

public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            Role role) {

        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }
}