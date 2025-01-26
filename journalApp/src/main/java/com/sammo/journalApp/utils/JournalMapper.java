package com.sammo.journalApp.utils;

import com.sammo.journalApp.DTO.JournalEntryRequest;
import com.sammo.journalApp.DTO.JournalEntryResponse;
import com.sammo.journalApp.DTO.MakeJournalCollaborativeRequest;
import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class JournalMapper {

    @Autowired
    private UserService userService;

    public JournalEntry mapRequestToJournalEntry(JournalEntryRequest request){

        JournalEntry journalEntry = new JournalEntry();

        journalEntry.setTitle(request.getTitle());
        journalEntry.setContent(request.getContent());

        boolean isCollaborative = Boolean.TRUE.equals(request.getIsCollaborative());
        journalEntry.setIsCollaborative(isCollaborative);

        if( isCollaborative ){

            List<String> collaborators = ( request.getCollaborators() != null ) ? new ArrayList<>(request.getCollaborators()) : new ArrayList<>();
            HashMap<String, String> permissions = ( request.getPermissions() != null ) ? new HashMap<>(request.getPermissions()) : new HashMap<>();

            journalEntry.setCollaborators(collaborators);
            journalEntry.setPermissions(permissions);
        } else{
            journalEntry.setCollaborators(new ArrayList<>());
            journalEntry.setPermissions(new HashMap<>());
        }

        return journalEntry;
    }

    public JournalEntryResponse mapJournalEntryToResponse(JournalEntry journalEntry){

        JournalEntryResponse journalEntryResponse = new JournalEntryResponse();

        journalEntryResponse.setId(journalEntry.getId().toString());
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
