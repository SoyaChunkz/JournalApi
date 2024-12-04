package com.sammo.journalApp.entitiy;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// POJO Class for Users
@Document(collection = "users")
@Data
public class User {

    @Id
    private ObjectId objectId;

    @Indexed(unique = true)
    @NonNull
    private String userName;

    @NonNull
    private String email;

    @NonNull
    private String password;

    @DBRef
    private List<JournalEntry> journalEntries;

    private List<String> roles;

    private LocalDateTime date;
}
