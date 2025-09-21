package com.example.springcrud.repository;

import com.example.springcrud.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable(value = "usersByUsername", key = "#username")
    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    Optional<Object> findByEmail( String email);
}
