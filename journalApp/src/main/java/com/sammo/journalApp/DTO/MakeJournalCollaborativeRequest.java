package com.sammo.journalApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeJournalCollaborativeRequest {

    private List<String> collaborators;
    private HashMap<String, String> permissions;
}
