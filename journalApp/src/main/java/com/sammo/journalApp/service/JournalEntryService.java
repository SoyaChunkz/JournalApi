package com.sammo.journalApp.service;

import com.sammo.journalApp.Repository.JournalEntryRepository;
import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.entitiy.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public boolean saveJournalEntry(JournalEntry myEntry, String myUserName){

        try {

            myEntry.setDate(LocalDateTime.now());

            JournalEntry savedEntry = journalEntryRepository.save(myEntry);

            Optional<User> userOpt = userService.searchUserByUsername(myUserName);

            if( userOpt.isPresent() ){

                User user = userOpt.get();

                user.getJournalEntries().add(savedEntry);
                userService.saveUser(user);

                return true;
            }
            else{
                return false;
            }
        }
        catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("Error occurred while saving the entry.", e);
        }
    }


    public void saveJournalEntry(JournalEntry myEntry){

        journalEntryRepository.save(myEntry);
    }

    public List<JournalEntry> getAllJournalEntries(){

        return journalEntryRepository.findAll();
    }

    public List<JournalEntry> getAllJournalEntriesByUserName(String myUserName){

        Optional<User> userOpt = userService.searchUserByUsername(myUserName);

        if( userOpt.isPresent() ){
            User user = userOpt.get();
            return user.getJournalEntries();
        }
        else{
            return null;
        }

    }

    public Optional<JournalEntry> getJournalEntryById(ObjectId myId){

        return journalEntryRepository.findById(myId);
    }

    public JournalEntry searchJournalEntryByTitle(String title, String myUserName){

        Optional<User> userOpt = userService.searchUserByUsername(myUserName);

        if( userOpt.isPresent() ){

            List<JournalEntry> allEntries = userOpt.get().getJournalEntries();

            if (allEntries == null || allEntries.isEmpty())
                return null;

            for( JournalEntry journalEntry : allEntries ){

                if( journalEntry.getTitle().equalsIgnoreCase(title) )
                    return journalEntry;
            }
        }

        return null;
    }

    public boolean deleteJournalEntryById(ObjectId myId, String myUserName){

        Optional<User> userOpt = userService.searchUserByUsername(myUserName);
        Optional<JournalEntry> journalEntryOpt = getJournalEntryById(myId);

        if( userOpt.isPresent() && journalEntryOpt.isPresent() ){

            JournalEntry journalEntry = journalEntryOpt.get();
            User user = userOpt.get();
            List<JournalEntry> allEntries = user.getJournalEntries();
            for (int i = 0; i < allEntries.size(); i++) {

                if (allEntries.get(i).equals(journalEntry)) {

                    allEntries.remove(i);
                    userService.saveUser(user);
                    journalEntryRepository.deleteById(myId);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isJournalAndUserNameValid(ObjectId myId, String myUserName){

        Optional<JournalEntry> journalEntryOpt = getJournalEntryById(myId);
        Optional<User> userOpt = userService.searchUserByUsername(myUserName);

        if( journalEntryOpt.isPresent() && userOpt.isPresent() ){

            JournalEntry journalEntry = journalEntryOpt.get();
            User user = userOpt.get();

            List<JournalEntry> allEntries = user.getJournalEntries();

            for( JournalEntry entry : allEntries ){

                if( entry.getId().equals(myId) )
                    return true;
            }
        }

        return false;
    }
}
