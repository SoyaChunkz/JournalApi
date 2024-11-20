package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/health-check")
    public String healthCheck(){

        return "OK";
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody User myUser){

        userService.saveNewUser(myUser, "USER");
        return new ResponseEntity<>("User Created Successfully", HttpStatus.CREATED);
    }
}
