package com.example.springcrud.config;

import com.example.springcrud.service.MyUserDetailsSerice;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;

    @Getter
    private final MyUserDetailsSerice userDetailsService;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String API_USERS_ID = "/api/users/{id}";



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for easier API testing (consider re-enabling for real apps)
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to registration and login endpoints
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()

                        // --- Role-Based Access Control ---
                        // Only users with the 'ADMIN' role can get all users
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole(ROLE_ADMIN)

                        // Only users with the 'ADMIN' role can update a user by username
                        .requestMatchers(HttpMethod.PUT, "/api/users/update").hasAnyRole(ROLE_ADMIN,"USER")


                        .requestMatchers(HttpMethod.DELETE, "/api/users/delete-direct").hasAnyRole(ROLE_ADMIN,"USER")
                        // Any authenticated user (ADMIN or USER) can get a single user by ID
                        // This allows a regular user to view their own profile, for example.
                       // .requestMatchers(HttpMethod.GET, API_USERS_ID).hasAnyRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()


                        //  .requestMatchers(HttpMethod.PUT, "api/users/{username}/roles").hasRole(ROLE_ADMIN)

                        // All other requests require authentication (as a fallback)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // For REST APIs (no sessions, use tokens)

                )
                .addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
                //.httpBasic(Customizer.withDefaults());

        return http.build();
    }


}