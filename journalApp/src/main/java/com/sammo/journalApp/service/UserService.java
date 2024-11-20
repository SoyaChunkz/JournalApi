package com.sammo.journalApp.service;


import com.sammo.journalApp.Repository.UserRepository;
import com.sammo.journalApp.entitiy.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    public void saveNewUser(User myUser, String role){

        myUser.setDate(LocalDateTime.now());
        myUser.setPassword(passwordEncoder.encode(myUser.getPassword()));

        if( ROLE_USER.equals(role) )
            myUser.setRoles(Arrays.asList(ROLE_USER));

        else if( ROLE_ADMIN.equals(role) )
            myUser.setRoles(Arrays.asList(ROLE_ADMIN, ROLE_USER));

        else
            throw new IllegalArgumentException("Invalid Role: " + role);
        userRepository.save(myUser);
    }

    public void saveUser(User myUser){

        userRepository.save(myUser);
    }

    public List<User> getAllUsers(){

        return userRepository.findAll();
    }

    public Optional<User> getUserById(ObjectId myId){

        return userRepository.findById(myId);
    }

    public Optional<User> searchUserByUsername(String myUserName){

        return userRepository.findByUserName(myUserName);
    }

    public void deleteUserById(ObjectId myId){

        userRepository.deleteById(myId);
    }

    public void deleteUSerByUserName(String myUserName){

        userRepository.deleteByUserName(myUserName);
    }
}
