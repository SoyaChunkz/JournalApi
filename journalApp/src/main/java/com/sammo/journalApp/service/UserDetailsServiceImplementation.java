package com.sammo.journalApp.service;

import com.sammo.journalApp.Repository.UserRepository;
import com.sammo.journalApp.entitiy.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("Attempting to load user by username: " + username);

        Optional<User> userOpt = userRepository.findByUserName(username);

        if( userOpt.isPresent() ){
            User user = userOpt.get();

            System.out.println("User found: " + user.getUserName());
            System.out.println("Encoded password: " + user.getPassword());
            System.out.println("User roles: " + user.getRoles());

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserName())
                    .password(user.getPassword())
                    .roles(user.getRoles().toArray(new String[0]))
                    .build();

            System.out.println("Returning UserDetails for: " + userDetails.getUsername() + " " + userDetails.getPassword());


            return userDetails;
        }

        throw new UsernameNotFoundException("User not found with this username: " + username);
    }
}
