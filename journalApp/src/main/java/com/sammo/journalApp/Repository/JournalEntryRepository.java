package com.sammo.journalApp.Repository;

import com.sammo.journalApp.entitiy.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface JournalEntryRepository extends MongoRepository<JournalEntry, ObjectId> {

    Optional<JournalEntry> findByTitle(String title);
}
