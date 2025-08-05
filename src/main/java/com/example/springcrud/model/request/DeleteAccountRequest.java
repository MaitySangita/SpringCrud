package com.example.springcrud.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {

    @NotBlank(message = "Username is required for deletion & cannot be null")
    private String username;

    @NotBlank(message = "Password is required for deletion ")
    private String password;

    private boolean confirmDeletion;
}
