package com.example.springcrud.service;

import com.example.springcrud.config.UserPrincipal;
import com.example.springcrud.exception.*;

import com.example.springcrud.entity.User;
import com.example.springcrud.model.request.DeleteAccountRequest;
import com.example.springcrud.model.request.UpdateUserRequest;
import com.example.springcrud.model.request.UserRequest;
import com.example.springcrud.model.response.UserResponse;
import com.example.springcrud.repository.UserRepository;

import com.example.springcrud.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @CachePut(value = "usersByUsername", key = "#registerRequest.username")
    public void registerUser(UserRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserIsPresentException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserIsPresentException("Email '" + registerRequest.getEmail() + "' is already registered.");
        }


        User user = new User();
        user.setFullname(registerRequest.getFullname());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(new HashSet<>(Collections.singletonList("USER")));

//        User user = User.builder()
//                .fullname(registerRequest.getFullname())
//                .username(registerRequest.getUsername())
//                .email(registerRequest.getEmail())
//                .password(passwordEncoder.encode(registerRequest.getPassword()))
//                .roles(new HashSet<>(Collections.singletonList("USER")))
//                .build();

        userRepository.save(user);
    }

    // --- R: Login / Authentication (This is for your custom /login endpoint's JSON body) ---

    public String loginAndGetToken(String username, String rawPassword) {

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty() || !passwordEncoder.matches(rawPassword, userOptional.get().getPassword())) {
                throw new InvalidCredentialsException("Invalid username or password."); // custom exception
            }


            return jwtUtil.generateToken(username);
    }



//    @Cacheable("users")
@Cacheable(value = "allUsers")
public List<UserResponse> getAllUsers() {

            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                throw new UserNotFoundException("No users found in the system.");
            }
            return users.stream()
                    .map(user -> {
                        UserResponse response = new UserResponse();
                        response.setId(user.getId());
                        response.setFullName(user.getFullname());
                        response.setUsername(user.getUsername());
                        response.setEmail(user.getEmail());
                        response.setRoles(user.getRoles());
                        return response;
                    })
                    .toList();

    }


    // --- R: Read (Retrieve user by ID) ---
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserProfileById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));


        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullname());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles());

        return response;
    }

            //--UPDATE--

    @CacheEvict(value = "usersByUsername", key = "#request.username")
    @Transactional
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


        //  --DELETE--

    @Caching(evict = {
            @CacheEvict(value = "allUsers", allEntries = true),
            @CacheEvict(value = "usersByUsername", key = "#request.username")
    })
    @Transactional
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
            throw new UserOperationException("Please confirm account deletion by setting 'confirmDeletion' to true.");
        }

        userRepository.delete(userToDelete);
    }


}