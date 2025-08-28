package com.example.springcrud.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class UserResponse implements Serializable {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Set<String> roles;
}
