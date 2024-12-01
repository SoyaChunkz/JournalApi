package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.JournalEntryService;
import com.sammo.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JournalEntryService journalEntryService;

    // Retrieve all users
    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUsers(){

        try{
            log.info("Fetching all users...");
            List<User> allUsers = userService.getAllUsers();

            if( !allUsers.isEmpty() ){
                log.info("Successfully fetched {} users.", allUsers.size());
                return new ResponseEntity<>(allUsers, HttpStatus.OK);
            }
            else{
                log.info("No users found in the database.");
                return new ResponseEntity<>("No Users", HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching users: {}", e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Retrieve all journal entries
    @GetMapping("/get-all-entries")
    public ResponseEntity<?> getAllJournalEntries(){

        try{
            log.info("Fetching all entries...");
            List<JournalEntry> allEntries = journalEntryService.getAllJournalEntries();

            if( allEntries != null ){
                log.info("Successfully fetched {} entries.", allEntries.size());
                return new ResponseEntity<>(allEntries, HttpStatus.OK);
            }
            else{
                log.info("No entries found in the database.");
                return new ResponseEntity<>("Journal is Empty", HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching entries: {}", e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Create a new admin
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody User myUser){

        try {
            if (myUser == null || myUser.getUserName().isEmpty()) {
                log.warn("Invalid admin input data: {}", myUser);
                return new ResponseEntity<>("Invalid input data", HttpStatus.BAD_REQUEST);
            }

            log.info("Creating admin account for: {}", myUser.getUserName());
            userService.saveNewUser(myUser, "ADMIN");
            log.info("Successfully created admin account for: {}", myUser.getUserName());
            return new ResponseEntity<>("Admin Created Successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("An error occurred while creating account: {}", e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
