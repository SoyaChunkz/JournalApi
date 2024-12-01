package com.sammo.journalApp.service;

import com.sammo.journalApp.Repository.UserRepository;
import com.sammo.journalApp.entitiy.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class UserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Attempting to load user by username: {}", username);

        Optional<User> userOpt = userRepository.findByUserName(username);

        if( userOpt.isPresent() ){
            User user = userOpt.get();

            log.debug("User found: {} for roles : {}", user.getUserName(), user.getRoles());

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserName())
                    .password(user.getPassword())
                    .roles(user.getRoles().toArray(new String[0]))
                    .build();

            log.info("Successfully returning UserDetails for user: {}", user.getUserName());

            return userDetails;
        }

        log.warn("User not found with username: {}", username);
        throw new UsernameNotFoundException("User not found with this username: " + username);
    }
}
