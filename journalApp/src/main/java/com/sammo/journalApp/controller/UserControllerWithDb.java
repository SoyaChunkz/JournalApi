package com.sammo.journalApp.controller;


import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/user")
public class UserControllerWithDb {

    @Autowired
    private UserService userService;

    // Retrieve a user by ID
    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getUserById(@PathVariable ObjectId myId){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        Optional<User> userOpt = userService.getUserById(myId);

        if( userOpt.isPresent() ){

            User user = userOpt.get();

            return (user.getUserName().equals(myUserName)) ?
                    new ResponseEntity<>(user, HttpStatus.OK) :
                    new ResponseEntity<>("Unauthorized", HttpStatus.FORBIDDEN);
        }
        else{
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        }
    }

    // Search user by userName
    @GetMapping("/userName")
    public ResponseEntity<?> getUserByUserName(){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        Optional<User> myUser = userService.searchUserByUsername(myUserName);

        if( myUser.isPresent() ){
            return new ResponseEntity<>(myUser, HttpStatus.OK);
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
    }

    // Delete a user by ID
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteUserById(@PathVariable ObjectId myId){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        Optional<User> userOpt = userService.getUserById(myId);

        if( userOpt.isPresent() ){
            User user = userOpt.get();

            if( user.getUserName().equals(myUserName) ){
                userService.deleteUserById(myId);
                return new ResponseEntity<>("User Deleted Successfully", HttpStatus.OK);
            }
            else
                return new ResponseEntity<>("Unauthorized", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
    }

    // Delete a user by userName
    @DeleteMapping
    public ResponseEntity<?> deleteUserByUserName(){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        if( userService.searchUserByUsername(myUserName).isPresent() ){
            userService.deleteUSerByUserName(myUserName);
            return new ResponseEntity<>("User Deleted Successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
    }

    // Update a user
    @PutMapping
    public ResponseEntity<?> updateUserById(@RequestBody User myUser){

        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String myUserName = authenticatedUser.getName();

        Optional<User> oldUserOpt = userService.searchUserByUsername(myUserName);

        if( oldUserOpt.isPresent() ){
            User oldUser = oldUserOpt.get();

            oldUser.setUserName( !myUser.getUserName().isEmpty() ? myUser.getUserName() : oldUser.getUserName());
            oldUser.setPassword( !myUser.getPassword().isEmpty() ? myUser.getPassword() : oldUser.getPassword());

            String role = oldUser.getRoles().get(0);
            userService.saveNewUser(oldUser, role);

            return new ResponseEntity<>("User Updated Successfully", HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
        }
    }
}
