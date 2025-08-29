package com.standingcat.event;

import com.standingcat.event.model.User;
import com.standingcat.event.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc

public class AuthTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        // Ensure a baseline user exists for login tests
        userRepository.save(new User(
                null,
                "jwtuser",
                passwordEncoder.encode("secret123"),
                "jwt@example.com",
                Set.of("ROLE_USER"),
                null,
                new HashSet<>()
        ));
    }

    @Test
    void registerUser_success() throws Exception {
        String newUserJson = """
            {
              "username": "newuser",
              "password": "newpass123",
              "email": "new@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.password").doesNotExist()) // password hidden
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }
    @Test
    void loginUser_success() throws Exception {

        String newUserJson = """
            {
              "username": "newuser",
              "password": "newpass123",
              "email": "new@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.password").doesNotExist()) // password hidden
                .andExpect(jsonPath("$.email").value("new@example.com"));


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\", \"password\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
    @Test
    void loginUser_invalidPassword() throws Exception {
        String newUserJson = """
            {
              "username": "badpassuser",
              "password": "rightpass",
              "email": "badpass@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isCreated());

        userRepository.flush();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"badpassuser\", \"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void loginUser_nonExistentUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\", \"password\":\"nopass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

}
