package com.standingcat.event;

import com.standingcat.event.model.User;
import com.standingcat.event.repository.UserRepository;
import com.standingcat.event.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // ensure no leftovers from other tests

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        userService.registerNewUser(user);
    }



    @Test
    void login_success_createsSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin("/login")
                        .user("username", "testuser")
                        .password("password", "password123"))
                .andExpect(authenticated().withUsername("testuser"))
                .andReturn();

        //get session directly from MockMvc
        var session = result.getRequest().getSession(false);
        assertNotNull(session, "Session should be created after login");

        //verify SPRING_SECURITY_CONTEXT is in session
        assertNotNull(session.getAttribute("SPRING_SECURITY_CONTEXT"),
                "Security context should be stored in session after login");
    }

    @Test
    void login_fail_invalidPassword() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("username", "testuser")
                        .password("password", "wrongpassword"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logout_success_invalidatesSession() throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", "testuser")
                        .password("password", "password123"))
                .andExpect(authenticated())
                .andReturn();

        HttpSession session = loginResult.getRequest().getSession(false);
        assertNotNull(session);

        MvcResult logoutResult = mockMvc.perform(post("/logout")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andReturn();

        HttpSession afterLogoutSession = logoutResult.getRequest().getSession(false);
        assertNull(afterLogoutSession, "Session should be invalidated after logout");
    }

}

