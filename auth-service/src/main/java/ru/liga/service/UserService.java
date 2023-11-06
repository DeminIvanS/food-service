package ru.liga.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import ru.liga.dto.RegistrationDto;
import ru.liga.model.CustomUserDetails;

import java.util.Collections;

@Service
public class UserService {

    private final UserDetailsService userDetailsService;

    @Qualifier("defaultPasswordEncoder")
    private final PasswordEncoder defaultPasswordEncoder;

    public UserService(UserDetailsService userDetailsService, PasswordEncoder defaultPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
    }

    public ResponseEntity<String> createUser(RegistrationDto request) {

        CustomUserDetails customUserDetails = new CustomUserDetails(
                request.getUsername(), defaultPasswordEncoder.encode(request.getPassword()),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        try {
            if (userDetailsService.loadUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A user with the same name already exists");
            }

        } catch (UsernameNotFoundException e) {
            ((JdbcUserDetailsManager) userDetailsService).createUser(customUserDetails);
            return ResponseEntity.ok("User successfully created");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");

    }
}
