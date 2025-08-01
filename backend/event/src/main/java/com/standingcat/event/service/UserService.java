package com.standingcat.event.service;

import com.standingcat.event.exception.EmailAlreadyRegisteredException;
import com.standingcat.event.exception.UsernameAlreadyTakenException;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(User user){

        //check if username and email is in repository already
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new UsernameAlreadyTakenException("Username is already taken!");
        }
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EmailAlreadyRegisteredException("Email is already taken.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        /* User savedUser = new User(
                1L, //fake id, any long
                newUser.getUsername(),
                encodedPassword,
                newUser.getEmail(),
                Set.of("ROLE_USER"),
                null,
                null
        ); */

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        User newUser = userRepository.save(user);

        return newUser;
    }

    public User createAdminUser(User user){
        return null;
    }
}
