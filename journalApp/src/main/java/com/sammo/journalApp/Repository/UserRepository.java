package com.sammo.journalApp.Repository;

import com.sammo.journalApp.entitiy.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByUserName(String myUserName);

    void deleteByUserName(String myUserName);
}
