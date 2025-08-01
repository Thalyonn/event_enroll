package com.standingcat.event.service;

import com.standingcat.event.model.User;
import com.standingcat.event.repository.EventRepository;
import com.standingcat.event.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock // Creates a mock instance of EventRepository
    private UserRepository userRepository;

    @Mock // Creates a mock instance of UserService (planned for UserService)
    private PasswordEncoder passwordEncoder;

    @InjectMocks //injects the above mocks into a new instance of UserService
    private UserService userService; //we are testing this

    private User newUser;
    private String rawPassword = "securePassword123";
    private String encodedPassword = "hashedSecurePassword123";

    @BeforeEach
    void setUp() {
        //initialize a User object that we'll use for testing registration.
        //ID is null because it's a new user not yet saved to the DB.
        //Roles are null because the service is responsible for setting them.
        newUser = new User(
                null,
                "newuser",
                rawPassword,
                "newuser@example.com",
                null,
                null,
                null
        );

        //mock the behavior of the passwordEncoder.encode method.
        //when encode is called with 'rawPassword', it should return 'encodedPassword'.
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
    }
    @Test
    void register_user_username_and_email_not_taken() {
        //if username is not taken, and email is not taken, user should be registered

        //mock username and email repository returning empty
        when(userRepository.findByUsername(newUser.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());

        User savedUser = new User(
                1L, //fake id, any long
                newUser.getUsername(),
                encodedPassword,
                newUser.getEmail(),
                Set.of("ROLE_USER"),
                null,
                null
        );
        //save any User.class as object passed to the service may change by the service
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        //call the actual method
        User registeredUser = userService.registerNewUser(newUser);

        //verify the calls in when were actually used and verify number of calls.
        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, times(1)).findByEmail(newUser.getEmail());
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));

        assertNotNull(registeredUser, "Registered user shouldn't be null");
        assertEquals(savedUser.getId(), registeredUser.getId(), "ID should be properly set by repository.");
        assertEquals(newUser.getUsername(), registeredUser.getUsername(), "Username should match");
        assertEquals(encodedPassword, registeredUser.getPassword(), "Password should be encoded");
        assertEquals(newUser.getEmail(), registeredUser.getEmail(), "Email should match");
        assertTrue(registeredUser.getRoles().contains("ROLE_USER"), "User should have ROLE_USER");
        assertEquals(1, registeredUser.getRoles().size(), "User should only have one role initially");


    }

    @Test
    void register_user_username_taken() {
        //if username is taken, user should not be registered
        when(userRepository.findByUsername(newUser.getUsername())).thenReturn(Optional.of(new User()));
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerNewUser(newUser),
                "Should throw IllegalArgumentException when username is taken"
        );
        //assert the exception message contains the expected text
        assertTrue(thrown.getMessage().contains("Username '" + newUser.getUsername() + "' is already taken."),
                "Exception message should indicate username is already taken");

        //verify that findByUsername was called, but other methods weren't called
        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, never()).findByEmail(anyString()); //email check shouldn't happen
        verify(passwordEncoder, never()).encode(anyString()); //password encoding should not happen
        verify(userRepository, never()).save(any(User.class)); //no user should be saved

    }

    @Test
    void register_email_taken() {
        //if username is taken, user should not be registered
        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.of(new User()));
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerNewUser(newUser),
                "Should throw IllegalArgumentException when email is taken"
        );
        //assert the exception message contains the expected text
        assertTrue(thrown.getMessage().contains("Email '" + newUser.getEmail() + "' is already taken."),
                "Exception message should indicate email is already taken");

        //verify that findByUsername was called, but other methods weren't called
        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, times(1)).findByEmail(newUser.getEmail());
        verify(passwordEncoder, never()).encode(anyString()); //password encoding should not happen
        verify(userRepository, never()).save(any(User.class)); //no user should be saved

    }

    //make test for admin user
}
