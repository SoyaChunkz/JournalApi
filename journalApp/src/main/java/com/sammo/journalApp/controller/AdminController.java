package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.JournalEntryService;
import com.sammo.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.oauth2.resourceserver.OAuth2ResourceServerSecurityMarker;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JournalEntryService journalEntryService;

    // Retrieve all users
    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUsers(){

        List<User> allUsers = userService.getAllUsers();

        if( !allUsers.isEmpty() ){
            return new ResponseEntity<>(allUsers, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("No Users", HttpStatus.NO_CONTENT);
        }
    }

    // Retrieve all journal entries
    @GetMapping("/get-all-entries")
    public ResponseEntity<?> getAllJournalEntries(){

        List<JournalEntry> allEntries = journalEntryService.getAllJournalEntries();

        if( allEntries != null ){
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Journal is Empty", HttpStatus.NO_CONTENT);
        }
    }

    // Create a new admin
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody User myUser){

        userService.saveNewUser(myUser, "ADMIN");
        return new ResponseEntity<>("Admin Created Successfully", HttpStatus.CREATED);
    }
}
