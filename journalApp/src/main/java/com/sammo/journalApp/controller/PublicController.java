package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.UserDetailsServiceImplementation;
import com.sammo.journalApp.service.UserService;
import com.sammo.journalApp.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/health-check")
    public String healthCheck(){

        return "UP and RUNNING";
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User myUser){

        boolean isUserCreated = userService.saveNewUser(myUser, "USER");
        if( isUserCreated ){
            try {
                UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername(myUser.getUserName());
                String jwt = jwtUtil.generateToken(userDetails);
                log.info("Successfully created user: {}", myUser.getUserName());
                log.info("{} successfully logged in.", myUser.getUserName());
                return new ResponseEntity<>(jwt, HttpStatus.CREATED);
            } catch (Exception e){
                return new ResponseEntity<>("Error generating token", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            log.error("Error creating user: {}", myUser.getUserName());
            return new ResponseEntity<>("User already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User myUser){

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(myUser.getUserName(), myUser.getPassword()));
            UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername(myUser.getUserName());
            String jwt = jwtUtil.generateToken(userDetails);
            log.info("{} successfully logged in.", myUser.getUserName());
            return new ResponseEntity<>(jwt, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.NOT_FOUND);
        }
    }
}
