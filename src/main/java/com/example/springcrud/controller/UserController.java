package com.example.springcrud.controller;


import com.example.springcrud.config.UserPrincipal;
import com.example.springcrud.model.request.DeleteAccountRequest;
import com.example.springcrud.model.request.LoginRequest;
import com.example.springcrud.model.request.UpdateUserRequest;
import com.example.springcrud.model.response.ApiResponse;
import com.example.springcrud.entity.User;
import com.example.springcrud.model.response.UserResponse;
import com.example.springcrud.service.UserService;
import com.example.springcrud.exception.UserNotFoundException;
import com.example.springcrud.model.request.UserRequest;

import com.example.springcrud.util.JwtUtil;
import jakarta.validation.Valid; // Import for @Valid annotation

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    //private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
       // this.jwtUtil = jwtUtil;
    }

    // --- Registration (Create) - NOW WITH VALIDATION AND DTO ---

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody UserRequest registerRequest) {
        userService.registerUser(registerRequest); // Pass DTO directly to service

        ApiResponse response = ApiResponse.builder()
                .message("Registration successful")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        String token = userService.loginAndGetToken(request.getUsername(), request.getPassword());

        ApiResponse response = ApiResponse.builder()
                .message("Login Successfully")
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }


//    // --- Retrieve All Users (Read) ---
//    @GetMapping
//    public ResponseEntity<?> getAllUsers() {
//        List<User> users = userService.getAllUsers();
//        if (users.isEmpty()) {
//            ApiResponse response = ApiResponse.builder().message("There are no user found").build();
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }
//        users.forEach(user -> user.setPassword(null)); // Remove passwords
//        return new ResponseEntity<>(users, HttpStatus.OK);
//    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        if (users.isEmpty()) {
            ApiResponse response = ApiResponse.builder().message("There are no users found").build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return ResponseEntity.ok(users);
    }


    // --- Retrieve User by ID (Read) ---
//    @GetMapping("/me")
//    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//        Long id = userPrincipal.getId();
//        return userService.getUserById(id)
//                .map(user -> {
//                    user.setPassword(null);
//                    return new ResponseEntity<>(user, HttpStatus.OK);
//                })
//                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
//    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse userProfile = userService.getUserProfileById(userPrincipal.getId());
        return ResponseEntity.ok(userProfile);
    }



    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUser(@Valid @RequestBody UpdateUserRequest request,
                                                  @AuthenticationPrincipal UserPrincipal loggedInUser) {

        // Move logic into service layer
        userService.updateUserByOwnerOrAdmin(request, loggedInUser);

        ApiResponse response = ApiResponse.builder()
                .message("Update successful")
                .build();

        return ResponseEntity.ok(response);
    }


//    // --- Delete User -
//    @DeleteMapping("/{username}")
//    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
//        userService.deleteUser(username);
//        return new ResponseEntity<>(" deleted successfully",HttpStatus.NO_CONTENT);
//    }


//    // --- DELETE My Account (self-deletion, username from authenticated context) ---
//    @DeleteMapping("/delete-direct") // New endpoint for direct deletion
//    public ResponseEntity<?> directDeleteUser(@Valid @RequestBody DeleteAccountRequest request) {
//
//        if (ObjectUtils.isEmpty(request.getUsername()) && ObjectUtils.isEmpty(request.getPassword())) {
//            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
//        }
//
//        userService.deleteUser(request); // Call the new service method
//
//        return new ResponseEntity<>( ApiResponse.builder().message("deleted successfully").build(), HttpStatus.NO_CONTENT);
//    }

    @DeleteMapping("/delete-direct")
    public ResponseEntity<ApiResponse> directDeleteUser(@Valid @RequestBody DeleteAccountRequest request,
                                                        @AuthenticationPrincipal UserPrincipal loggedInUser) {

        userService.deleteUser(request, loggedInUser); // Ensure auth context is used

        return new ResponseEntity<>(ApiResponse.builder().message("Account deleted successfully").build(), HttpStatus.NO_CONTENT);
    }


}