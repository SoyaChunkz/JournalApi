package com.sammo.journalApp.controller;

import com.sammo.journalApp.entitiy.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/_journal")
public class JournalEntryController {

    private Map<ObjectId, JournalEntry> journalEntries = new HashMap<>();

    @GetMapping
    public List<JournalEntry> getAll (){
        return new ArrayList<>(journalEntries.values());
    }

    @GetMapping("/id/{myId}")
    public JournalEntry getJournalEntryById(@PathVariable ObjectId myId){
        return journalEntries.get(myId);
    }

    @GetMapping("/search")
    public JournalEntry searchJournalEntry(@RequestParam String title){
        for ( JournalEntry j : journalEntries.values() ){

            if( title.equals(j.getTitle()) )
                return j;
        }

        return null;
    }

    @PostMapping
    public boolean createJournalEntry(@RequestBody JournalEntry myEntry){
        journalEntries.put(myEntry.getId(), myEntry);
        return true;
    }

    @DeleteMapping("/id/{myId}")
    public boolean deleteJournalEntryById(@PathVariable ObjectId myId){
        journalEntries.remove(myId);
        return true;
    }

    @PutMapping("/id/{myId}")
    public boolean updateJournalEntryById(@PathVariable ObjectId myId, @RequestBody JournalEntry myEntry){
        journalEntries.put(myId, myEntry);
        return true;
    }
}
