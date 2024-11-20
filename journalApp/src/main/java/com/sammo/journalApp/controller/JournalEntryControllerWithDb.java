package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.service.JournalEntryService;
import com.sammo.journalApp.service.UserService;
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
public class JournalEntryControllerWithDb {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    // Retrieve all journal entries by userName
    @GetMapping()
    public ResponseEntity<?> getAllJournalEntriesByUserName(){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        List<JournalEntry> allEntries = journalEntryService.getAllJournalEntriesByUserName(myUserName);

        if( allEntries != null ){
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Journal is Empty", HttpStatus.NO_CONTENT);
        }
    }

    // Retrieve a journal entry by ID
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable ObjectId myId){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        Optional<JournalEntry> journalEntryOpt = journalEntryService.getJournalEntryById(myId);

        if( journalEntryOpt.isEmpty() )
            return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);

        else if( !journalEntryService.isJournalAndUserNameValid(myId, myUserName) )
            return new ResponseEntity<>("Unauthorised", HttpStatus.FORBIDDEN);

        return new ResponseEntity<>(journalEntryOpt.get(), HttpStatus.OK);
    }

    // Search journal entry by title
    @GetMapping("/search")
    public ResponseEntity<?> searchJournalEntry(@RequestParam String title){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        JournalEntry journalEntry = journalEntryService.searchJournalEntryByTitle(title, myUserName);

        if( journalEntry != null )

            return new ResponseEntity<>(journalEntry, HttpStatus.OK);

        return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
    }

    // Create a new journal entry
    @PostMapping
    public ResponseEntity<?> createJournalEntry(@RequestBody JournalEntry myEntry){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        try {
            boolean isSaved = journalEntryService.saveJournalEntry(myEntry, myUserName);

            return (isSaved) ?
                    new ResponseEntity<>("Journal Entry Created Successfully", HttpStatus.CREATED) :
                    new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
        }

    }

    // Delete a journal entry by ID
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable ObjectId myId){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        boolean isValid = journalEntryService.isJournalAndUserNameValid(myId, myUserName);

        if( isValid ){

            boolean isDeleted = journalEntryService.deleteJournalEntryById(myId, myUserName);

            return (isDeleted) ?
                    new ResponseEntity<>("Journal Entry Deleted Successfully", HttpStatus.OK) :
                    new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
        }

        else
            return new ResponseEntity<>("Unauthorised", HttpStatus.FORBIDDEN);

    }

    // Update a journal entry by ID
    @PutMapping("/id/{myId}")
    public ResponseEntity<?> updateJournalEntryById(@PathVariable ObjectId myId, @RequestBody JournalEntry myEntry){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        boolean isValid = journalEntryService.isJournalAndUserNameValid(myId, myUserName);

        Optional<JournalEntry> oldEntryOpt = journalEntryService.getJournalEntryById(myId);

        if( isValid && oldEntryOpt.isPresent() && userService.searchUserByUsername(myUserName).isPresent() ){

            JournalEntry oldEntry = oldEntryOpt.get();
            oldEntry.setTitle( !myEntry.getTitle().isEmpty() ? myEntry.getTitle() : oldEntry.getTitle());
            oldEntry.setContent( ( myEntry.getContent() != null && !myEntry.getContent().isEmpty()) ? myEntry.getContent() : oldEntry.getTitle() );

            journalEntryService.saveJournalEntry(oldEntry);

            return new ResponseEntity<>("Journal Entry Updated Successfully", HttpStatus.OK);
        }

        else if( !isValid )
            return new ResponseEntity<>("Unauthorised", HttpStatus.FORBIDDEN);

        else if( userService.searchUserByUsername(myUserName).isEmpty() )
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);

        else
            return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);

    }
}
