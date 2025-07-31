package com.standingcat.event.service;

import com.standingcat.event.model.User;
import com.standingcat.event.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User registerNewUser(User user){
        return null;
    }

    public User createAdminUser(User user){
        return null;
    }
}
