package com.sammo.journalApp.DTO;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class JournalEntryResponse {

    private String id;

    private String title;

    private String content;

    private Boolean isCollaborative;

    private List<String> collaborators;

    private HashMap<String, String> permissions;

}
