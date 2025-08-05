package com.example.springcrud.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class UserResponse implements Serializable {

    private Long id;
    private String fullname;
    private String username;
    private String email;
    private Set<String> roles;
}
