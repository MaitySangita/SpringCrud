package com.example.springcrud.service;

import com.example.springcrud.config.UserPrincipal;
import com.example.springcrud.exception.InvalidCredentialsException;
import com.example.springcrud.exception.InvalidInputException;
import com.example.springcrud.exception.UserNotFoundException;

import com.example.springcrud.entity.User;
import com.example.springcrud.exception.UserOperationException;
import com.example.springcrud.model.request.DeleteAccountRequest;
import com.example.springcrud.model.request.UpdateUserRequest;
import com.example.springcrud.model.request.UserRequest;
import com.example.springcrud.model.response.UserResponse;
import com.example.springcrud.repository.UserRepository;

import com.example.springcrud.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service

public class UserService  {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    //---C: Registration
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user", key = "#registerRequest.username"),
            @CacheEvict(value = "userByUsername", key = "#registerRequest.username"),
            @CacheEvict(value = "authUserDetails", key = "#registerRequest.username")
    })
    public void registerUser(UserRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserNotFoundException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }

        User user = new User();
        user.setFullname(registerRequest.getFullname());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(new HashSet<>(Collections.singletonList("USER")));

        userRepository.save(user);
    }

//    // --- R: Login / Authentication (This is for your custom /login endpoint's JSON body) ---
//    public Optional<User> authenticateUser(String username, String rawPassword) {
//        try {
//            Optional<User> userOptional = userRepository.findByUsername(username);
//
//            if (userOptional.isPresent() && passwordEncoder.matches(rawPassword, userOptional.get().getPassword())) {
//                return userOptional;
//            }
//            return Optional.empty();  // Authentication failed
//        } catch (Exception e) {
//            throw new UserOperationException("Error during authentication: " + e.getMessage());
//        }
//    }

    public String loginAndGetToken(String username, String rawPassword) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty() || !passwordEncoder.matches(rawPassword, userOptional.get().getPassword())) {
                throw new InvalidCredentialsException("Invalid username or password."); // custom exception
            }

            return jwtUtil.generateToken(username);
        } catch (Exception e) {
            throw new UserOperationException("Error during login: " + e.getMessage());
        }
    }



    // --- R: Read (Retrieve all users) ---
//    @Cacheable(value = "users")
//    public List<User> getAllUsers() {
//        try {
//            return userRepository.findAll();
//        } catch (Exception e) {
//            throw new UserOperationException("Failed to fetch users: " + e.getMessage());
//        }
//    }

    @Cacheable(value = "user")
    public List<UserResponse> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            return users.stream()
                    .map(user -> {
                        UserResponse response = new UserResponse();
                        response.setId(user.getId());
                        response.setFullname(user.getFullname());
                        response.setUsername(user.getUsername());
                        response.setEmail(user.getEmail());
                        response.setRoles(user.getRoles());
                        return response;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new UserOperationException("Failed to fetch users: " + e.getMessage());
        }
    }


    // --- R: Read (Retrieve user by ID) ---
//    @Cacheable(value = "user", key = "#id")
//    public Optional<User> getUserById(Long id) {
//        try {
//            return userRepository.findById(id);
//        } catch (UserOperationException e) {
//            throw new UserOperationException("Failed to fetch user: " + e.getMessage());
//        }
//    }

    // --- R: Read (Retrieve user by ID) ---
    @Cacheable(value = "user", key = "#id")
    public UserResponse getUserProfileById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Convert to DTO (or use a mapper like MapStruct if preferred)
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullname(user.getFullname());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles());

        return response;
    }

//    @CacheEvict(value = {"users", "user", "userByUsername", "authUserDetails"}, allEntries = true)
//    @CachePut(value = "userByUsername", key = "#request.username")
//    public User updateUser(@Valid UpdateUserRequest request) {
//        if (request == null) {
//            throw new InvalidInputException("Invalid input");
//        }
//
//        try {
//            User existingUser = userRepository.findByUsername(request.getUsername())
//                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + request.getUsername()));
//
//            existingUser.setFullname(request.getFullname());
//            existingUser.setEmail(request.getEmail());
//            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
//
//            return userRepository.save(existingUser);
//        } catch (Exception e) {
//            throw new UserOperationException("Failed to update user: " + e.getMessage());
//        }
//    }

    //--UPDATE--

    @CacheEvict(value = {"users", "user", "userByUsername", "authUserDetails"}, allEntries = true)
    @CachePut(value = "userByUsername", key = "#request.username")
    public void updateUserByOwnerOrAdmin(UpdateUserRequest request, UserPrincipal loggedInUser) {
        if (request == null) {
            throw new InvalidInputException("Invalid input");
        }

        boolean isOwner = request.getUsername().equals(loggedInUser.getUsername());
        boolean isAdmin = loggedInUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this user.");
        }

        try {
            User existingUser = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + request.getUsername()));

            existingUser.setFullname(request.getFullname());
            existingUser.setEmail(request.getEmail());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(existingUser);

        } catch (Exception e) {
            throw new UserOperationException("Failed to update user: " + e.getMessage());
        }
    }


    // --- D: Delete User ---
//    public void deleteUser(String username) {
//        User userToDelete = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
//
//        userRepository.delete(userToDelete);
//    }

    // --- DELETE User (for authenticated user - no path variable) ---
//    @Caching(evict = {
//            @CacheEvict(value = "users", allEntries = true),
//            @CacheEvict(value = "user", key = "#request.username"),
//            @CacheEvict(value = "userByUsername", key = "#request.username"),
//            @CacheEvict(value = "authUserDetails", key = "#request.username")
//    })
//    public void deleteUser(DeleteAccountRequest request, UserPrincipal loggedInUser) {
//
//        try {
//            User userToDelete = userRepository.findByUsername(request.getUsername())
//                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + request.getUsername()));
//
//            // Verify the password provided in the request
//            if (!passwordEncoder.matches(request.getPassword(), userToDelete.getPassword())) {
//                throw new UserNotFoundException("Incorrect password provided for account deletion.");
//            }
//
//            if (!request.isConfirmDeletion()) {
//                throw new InvalidInputException("Please confirm that you understand the consequences of account deletion.");
//            }
//
//            userRepository.delete(userToDelete);
//        } catch (Exception e) {
//            throw new UserOperationException("Failed to delete user: " + e.getMessage());
//        }
//    }

    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user", key = "#request.username"),
            @CacheEvict(value = "userByUsername", key = "#request.username"),
            @CacheEvict(value = "authUserDetails", key = "#request.username")
    })
    public void deleteUser(DeleteAccountRequest request, UserPrincipal loggedInUser) {

        // Only allow self-deletion (extra protection)
        if (!request.getUsername().equals(loggedInUser.getUsername())) {
            throw new AccessDeniedException("You can only delete your own account.");
        }

        User userToDelete = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), userToDelete.getPassword())) {
            throw new InvalidInputException("Incorrect password for account deletion.");
        }

        if (!request.isConfirmDeletion()) {
            throw new InvalidInputException("Please confirm account deletion by setting 'confirmDeletion' to true.");
        }

        userRepository.delete(userToDelete);
    }


}