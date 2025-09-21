package com.example.springcrud.service;

import com.example.springcrud.config.UserPrincipal;
import com.example.springcrud.exception.UserOperationException;
import com.example.springcrud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username
            return userRepository.findByUsername(username)
                    // If found, use the static build method in UserPrincipal to create your custom UserDetails object
                    .map(UserPrincipal::build)
                    // If not found, throw the standard UsernameNotFoundException
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

    }

}
