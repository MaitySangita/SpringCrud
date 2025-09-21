package com.example.springcrud.config;

import com.example.springcrud.entity.User; // Import your User entity
import com.fasterxml.jackson.annotation.JsonIgnore; // For ignoring password in serialization
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * This class holds authenticated user information and their authorities (roles).
 * It allows storing more details from your User entity (like ID, email)
 * directly in the security context, providing convenient access after authentication.
 */
public class UserPrincipal implements UserDetails {

    // --- Getters for custom user-specific fields ---
    // These methods provide easy access to user details after authentication
    // without needing to query the database again.
    @Getter
    private Long id;
    private final String username;
    @Getter
    private String email;

    // Use @JsonIgnore to ensure the hashed password is not accidentally
    // serialized into JSON responses if this object is ever exposed directly.
    @JsonIgnore
    private String password;

    // Collection of authorities (roles) granted to the user.
    private final Collection<? extends GrantedAuthority> authorities;


    /**
     * Private constructor to ensure instances are created using the static build method.
     * @param id The unique ID of the user.
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The hashed password of the user.
     * @param authorities The collection of GrantedAuthority (roles) for the user.
     */
    private UserPrincipal(Long id, String username, String email, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Static factory method to create a UserPrincipal from your User entity.
     * This is the recommended way to convert your database User object into a
     * UserPrincipal for Spring Security.
     *
     * @param user The User entity fetched from the database.
     * @return A new UserPrincipal instance.
     */
    public static UserPrincipal build(User user) {
        // Convert the Set<String> roles from your User entity into
        // a List of SimpleGrantedAuthority objects.
        // Spring Security's hasRole() expects roles to be prefixed with "ROLE_".
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // This should be the HASHED password from the DB
                authorities
        );
    }

    // --- Implementation of UserDetails interface methods ---

    /**
     * Returns the authorities (roles) granted to the user.
     * @return A collection of GrantedAuthority objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the hashed password used to authenticate the user.
     * @return The user's hashed password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * @return The user's username.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     * We return true by default, but you can implement custom logic if needed.
     * @return true if the user's account is valid (i.e., not expired), false otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * We return true by default, but you can implement custom logic if needed.
     * @return true if the user is not locked, false otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * We return true by default, but you can implement custom logic if needed.
     * @return true if the user's credentials are valid (i.e., not expired), false otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * We return true by default, but you can implement custom logic if needed.
     * @return true if the user is enabled, false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Standard equals and hashCode for proper comparison ---
    // Essential for Spring Security to correctly manage authenticated principals.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        // Compare by ID for uniqueness as it's the primary key
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


