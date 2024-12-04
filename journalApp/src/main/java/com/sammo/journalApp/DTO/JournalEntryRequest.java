package com.sammo.journalApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryRequest {

    @NonNull
    private String title;
    private String content;
    private Boolean isCollaborative;
    private List<String> collaborators;
    private HashMap<String, String> permissions;
}
