package com.sammo.journalApp.controller;

import com.sammo.journalApp.DTO.JournalEntryRequest;
import com.sammo.journalApp.DTO.JournalEntryResponse;
import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.service.JournalEntryService;
import com.sammo.journalApp.service.UserService;
import com.sammo.journalApp.utils.JournalMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/journal")
@Slf4j
public class JournalEntryControllerWithDb {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @Autowired
    private JournalMapper journalMapper;

    // Retrieve all journal entries by userName✅
    @GetMapping()
    public ResponseEntity<?> getAllJournalEntriesByUserName(){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String myUserName = authenticatedUser.getName();

        try {
            log.info("Fetching all journal entries for: '{}'", myUserName);
            List<JournalEntryResponse> allEntries = journalEntryService.getAllJournalEntriesByUserName(myUserName);

            if( allEntries != null ){
                log.info("Successfully fetched '{}' entries for: '{}'", allEntries.size(), myUserName);
                return new ResponseEntity<>(allEntries, HttpStatus.OK);
            }
            else{
                log.info("No entries for '{}' in database.", myUserName);
                return new ResponseEntity<>("Journal is Empty", HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching journal entries for '{}': {}", myUserName, e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Retrieve a journal entry by ID✅
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable ObjectId myId){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String myUserName = authenticatedUser.getName();

        try {
            log.info("Fetching journal entry with ID '{}' for user: '{}'", myId, myUserName);
            JournalEntryResponse journalEntryResponse = journalEntryService.getJournalEntryById(myId);

            if( !journalEntryService.isUserCollaborator(myId, myUserName) ){
                log.info("Unauthorized access attempt detected by: '{}'", myUserName);
                return new ResponseEntity<>("Unauthorised", HttpStatus.FORBIDDEN);
            }
            else if( journalEntryResponse == null ){
                log.info("No journal entry found with ID: {}", myId);
                return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
            }

            else {
                log.info("Successfully fetched journal entry with ID '{}' for user: '{}'", myId, myUserName);
                return new ResponseEntity<>(journalEntryResponse, HttpStatus.OK);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid ID format: '{}'", myId, e);
            return new ResponseEntity<>("Invalid ID Format", HttpStatus.BAD_REQUEST);

        } catch (Exception e){
            log.error("An error occurred while fetching journal entry with ID '{}' for user '{}': {}", myId, myUserName, e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search journal entry by title✅
    @GetMapping("/search")
    public ResponseEntity<?> searchJournalEntry(@RequestParam String title){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String myUserName = authenticatedUser.getName();

        try {
            log.info("User '{}' is searching for journal entry with title: '{}'", myUserName, title);

            List<JournalEntryResponse> journalEntriesResponse = journalEntryService.searchJournalEntriesByTitle(title, myUserName);

            if( journalEntriesResponse != null ) {
                log.info("Journal entries found for user '{}': {}", myUserName, journalEntriesResponse);
                return new ResponseEntity<>(journalEntriesResponse, HttpStatus.OK);
            }
            else{
                log.info("No journal entry found with title '{}' for user '{}'", title, myUserName);
                return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("An error occurred while searching for journal entry with title '{}' for user '{}': {}", title, myUserName, e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Create a new journal entry✅
    @PostMapping
    public ResponseEntity<?> createJournalEntry(@RequestBody JournalEntryRequest request){

        log.info("Received request: {}", request);

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String creatorUserName = authenticatedUser.getName();

        try {
            log.info("User '{}' is attempting to create a new journal entry with title: '{}'", creatorUserName, request.getTitle());

            if( Boolean.TRUE.equals(request.getIsCollaborative()) ){

                if( request.getCollaborators() == null || request.getCollaborators().isEmpty() ){
                    log.warn("Crating Collaborative entry with one collaborator (Owner).");
                }
            }

            JournalEntry journalEntry = journalMapper.mapRequestToJournalEntry(request);

            boolean isSaved = journalEntryService.saveNewJournalEntry(journalEntry, creatorUserName);

            if (isSaved) {
                log.info("Journal entry '{}' created successfully for user '{}'.", request.getTitle(), creatorUserName);
                return new ResponseEntity<>("Journal Entry Created Successfully", HttpStatus.CREATED);
            } else {
                log.warn("Failed to create journal entry for user '{}': User not found.", creatorUserName);
                return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
                log.error("An error occurred while creating a journal entry for user '{}': {}", creatorUserName, e.getMessage(), e);
                return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
            }
    }

    // Delete a journal entry by ID✅
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable ObjectId myId) {

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String myUserName = authenticatedUser.getName();

        try {
            log.info("User '{}' is attempting to delete journal entry with ID: {}", myUserName, myId);

            boolean isValid = journalEntryService.isCollaboratorTheOwner(myId, myUserName);

            if( isValid ){
                boolean isDeleted = journalEntryService.deleteJournalEntryById(myId, myUserName);

                if( isDeleted ){
                    log.info("Journal entry with ID '{}' deleted successfully by user '{}'.", myId, myUserName);
                    return new ResponseEntity<>("Journal Entry Deleted Successfully", HttpStatus.OK);
                } else {
                    log.warn("Journal entry with ID '{}' not found for user '{}'.", myId, myUserName);
                    return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
                }
            } else {
                log.warn("Unauthorized attempt to delete journal entry with ID '{}' by user '{}'.", myId, myUserName);
                return new ResponseEntity<>("Unauthorized", HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("An error occurred while deleting journal entry with ID '{}' for user '{}': {}", myId, myUserName, e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //Update a journal entry by ID✅
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateJournalEntryById(@PathVariable ObjectId myId, @RequestBody JournalEntryRequest request){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();

        if( authenticatedUser == null || authenticatedUser.getName() == null ){
            log.warn("Unauthorized access attempt detected.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String myUserName = authenticatedUser.getName();

        try {
            log.info("User '{}' is attempting to update journal entry with ID: {}", myUserName, myId);

            JournalEntryResponse oldEntry = journalEntryService.getJournalEntryById(myId);
            boolean isValid = journalEntryService.isUserCollaborator(myId, myUserName);

            if( isValid && oldEntry != null && userService.searchUserByUsername(myUserName).isPresent() ){

                JournalEntry journalEntry = journalMapper.mapRequestToJournalEntry(request);
                boolean isUpdated = journalEntryService.updateJournalEntry(myId, oldEntry, journalEntry);

                if ( isUpdated ){
                    log.info("Journal entry with ID '{}' updated successfully by user '{}'.", myId, myUserName);
                    return new ResponseEntity<>("Journal Entry Updated Successfully", HttpStatus.OK);
                } else {
                    log.warn("Failed to update journal entry with ID '{}'.", myId);
                    return new ResponseEntity<>("Update Failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else if( !isValid ){
                log.warn("Unauthorized attempt to update journal entry with ID '{}' by user '{}'.", myId, myUserName);
                return new ResponseEntity<>("Unauthorized", HttpStatus.FORBIDDEN);
            } else if( userService.searchUserByUsername(myUserName).isEmpty() ){
                log.warn("User '{}' not found in the database.", myUserName);
                return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
            } else {
                log.warn("Journal entry with ID '{}' not found for user '{}'.", myId, myUserName);
                return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("An error occurred while updating journal entry with ID '{}' for user '{}': {}", myId, myUserName, e.getMessage(), e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
