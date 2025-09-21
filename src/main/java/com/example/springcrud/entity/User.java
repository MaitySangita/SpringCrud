package com.example.springcrud.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users") // Good practice to explicitly name your table
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@ToString(exclude = "password") // Lombok: Generates toString, exclude password for security
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fullname;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

   // @JsonIgnore // Ensures password is not serialized in JSON responses
    @Column(nullable = false)
    private String password;

    // Roles for the user (e.g., "ADMIN", "USER")
    // @ElementCollection maps a collection of simple types to a separate table
// Roles for the user (e.g., "ADMIN", "USER")

// @ElementCollection maps a collection of simple types to a separate table

    @ElementCollection(fetch = FetchType.EAGER) // Fetch roles eagerly when User is loaded

    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // Join table for roles

    @Column(name = "role") // Column in 'user_roles' table
    private Set<String> roles = new HashSet<>();

    // Custom constructor for easier object creation
    public User(String fullname, String username, String email, String password) {
        this.fullname=fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles.add("USER"); // Assign default 'USER' role when new user is created via this constructor
    }


}