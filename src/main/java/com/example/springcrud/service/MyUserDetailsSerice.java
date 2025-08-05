package com.example.springcrud.service;

import com.example.springcrud.config.UserPrincipal;
import com.example.springcrud.exception.UserOperationException;
import com.example.springcrud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service

public class MyUserDetailsSerice implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public MyUserDetailsSerice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username
        try {
            return userRepository.findByUsername(username)
                    // If found, use the static build method in UserPrincipal to create your custom UserDetails object
                    .map(UserPrincipal::build)
                    // If not found, throw the standard UsernameNotFoundException
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        } catch (Exception e) {
            throw new UserOperationException("Error loading user: " + e.getMessage());
        }
    }

}
