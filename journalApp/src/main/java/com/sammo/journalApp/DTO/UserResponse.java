package com.sammo.journalApp.DTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {

    private String userName;

    private String email;

    private List<JournalEntryResponse> journalEntries;

    private List<String> roles;

}
