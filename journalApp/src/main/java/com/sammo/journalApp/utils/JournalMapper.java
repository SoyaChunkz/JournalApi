package com.sammo.journalApp.utils;

import com.sammo.journalApp.DTO.JournalEntryRequest;
import com.sammo.journalApp.DTO.JournalEntryResponse;
import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JournalMapper {

    @Autowired
    private UserService userService;

    public JournalEntry mapRequestToJournalEntry(JournalEntryRequest request){

        JournalEntry journalEntry = new JournalEntry();

        journalEntry.setTitle(request.getTitle());
        journalEntry.setContent(request.getContent());
        journalEntry.setIsCollaborative(request.getIsCollaborative());


        if( request.getIsCollaborative() != null && request.getIsCollaborative() ){
            List<String> collaborators = new ArrayList<>(request.getCollaborators());
            journalEntry.setCollaborators(collaborators);

            HashMap<String, String> permissions = new HashMap<>(request.getPermissions());
            journalEntry.setPermissions(permissions);
        }

        return journalEntry;
    }

    public JournalEntryResponse mapJournalEntryToResponse(JournalEntry journalEntry){

        JournalEntryResponse journalEntryResponse = new JournalEntryResponse();

        journalEntryResponse.setTitle(journalEntry.getTitle());
        journalEntryResponse.setContent(journalEntry.getContent());
        journalEntryResponse.setIsCollaborative(journalEntry.getIsCollaborative());

        List<String> collaborators = new ArrayList<>(journalEntry.getCollaborators());
        journalEntryResponse.setCollaborators(collaborators);

        HashMap<String, String> permissions = new HashMap<>(journalEntry.getPermissions());

        journalEntryResponse.setPermissions(permissions);

        return journalEntryResponse;
    }

    public JournalEntry mapJournalEntryResponseToJournalEntry(JournalEntryResponse response) {

        JournalEntry journalEntry = new JournalEntry();

        journalEntry.setTitle(response.getTitle());
        journalEntry.setContent(response.getContent());
        journalEntry.setIsCollaborative(response.getIsCollaborative());

        if( response.getIsCollaborative() != null && response.getIsCollaborative() ){
            List<String> collaborators = new ArrayList<>(response.getCollaborators());
            journalEntry.setCollaborators(collaborators);

            HashMap<String, String> permissions = new HashMap<>(response.getPermissions());
            journalEntry.setPermissions(permissions);
        }

        return journalEntry;
    }
}
