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
import java.util.Optional;
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

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);
        User newUser = userRepository.save(user);

        return newUser;
    }

    @Transactional
    public User createAdminUser(User user){
        //check if username and email is in repository already
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new UsernameAlreadyTakenException("Username is already taken!");
        }
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EmailAlreadyRegisteredException("Email is already taken.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        user.setRoles(roles);
        User newUser = userRepository.save(user);

        return newUser;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
