package com.sammo.journalApp.service;

import com.mongodb.client.result.UpdateResult;
import com.sammo.journalApp.DTO.JournalEntryResponse;
import com.sammo.journalApp.DTO.MakeJournalCollaborativeRequest;
import com.sammo.journalApp.Repository.JournalEntryRepository;
import com.sammo.journalApp.entitiy.JournalEntry;
import com.sammo.journalApp.entitiy.User;
import com.sammo.journalApp.utils.JournalMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JournalMapper journalMapper;

    private static final Set<String> VALID_PERMISSIONS = Set.of("OWNER", "READ", "WRITE");


    @Transactional
    public boolean saveNewJournalEntry(JournalEntry myEntry, String creatorUserName){

        try {
            // 1. set the date
            myEntry.setDate(LocalDateTime.now());

            // 2. fetch the creator
            Optional<User> creatorOpt = userService.searchUserByUsername(creatorUserName);
            if( creatorOpt.isEmpty() ){
                throw new IllegalArgumentException("Creator user not found.");
            }
            User creator = creatorOpt.get();

            // 3. initialize collaborators list with the creator
            List<String> collaborators = new ArrayList<>();
            collaborators.add(creatorUserName);

            // 4. initialize permissions map and set the creator's OWNER permission
            HashMap<String, String> permissions = new HashMap<>(myEntry.getPermissions());
            permissions.put(creatorUserName, "OWNER");

            // 5. if collaborative, validate collaborators
            if( Boolean.TRUE.equals(myEntry.getIsCollaborative()) ){

                // retrieve all collaborators' userNames
                List<String> userNamesOfAdditionalCollaborators = new ArrayList<>(myEntry.getCollaborators());

                // get the collaborators from DB
                List<User> additionalCollaborators = (!userNamesOfAdditionalCollaborators.isEmpty()) ?
                        userService.getUsersByUsernames(userNamesOfAdditionalCollaborators) :
                        new ArrayList<>();

                // add the collaborators to the list
                for( User collaborator : additionalCollaborators ){
                    if( !collaborators.contains(collaborator.getUserName()) ){
                        collaborators.add(collaborator.getUserName());
                    }

                    // set default READ permission for all additional collaborators if not already set
                    permissions.putIfAbsent(collaborator.getUserName(), "READ");
                }

                for( Map.Entry<String, String> entry : permissions.entrySet() ){

                    if( !entry.getKey().equals(creatorUserName) && entry.getValue().equals("OWNER") ){
                        permissions.put(entry.getKey(), "READ");
                    }
                }
                List<User> users = userService.getUsersByUsernames(new ArrayList<>(permissions.keySet()));

                Set<String> existingUsernames = new HashSet<>();
                for (User user : users) {
                    existingUsernames.add(user.getUserName());
                }

                for( String collaborator : permissions.keySet() ){
                    if( !collaborators.contains(collaborator) ){
                        if( existingUsernames.contains(collaborator) ){
                            collaborators.add(collaborator);
                        } else {
                            throw new IllegalArgumentException("Collaborator Not Found. " + collaborator);
                        }
                    }
                }

            }

            // 4. set the collaborators and permissions
            myEntry.setCollaborators(collaborators);
            myEntry.setPermissions(permissions);

            // 5. validate the permissions
            validatePermissions(myEntry.getPermissions(), collaborators);

            // 6. save the validated journal entry
            JournalEntry savedEntry = journalEntryRepository.save(myEntry);

            // 7. link the creator with the journal entry
            System.out.println(creator.getJournalEntries());
            creator.getJournalEntries().add(savedEntry);
            userService.saveUser(creator);

            return true;
        }
        catch (Exception e){
            throw new RuntimeException("Error occurred while saving the entry.", e);
        }
    }

    private void validatePermissions(HashMap<String, String> permissions, List<String> collaborators){

        for( String collaborator : collaborators ){

            if( !permissions.containsKey(collaborator) ){
                throw new IllegalArgumentException("Missing permission for collaborator: " + collaborator);
            }

            String permission = permissions.get(collaborator);
            if( !isValidPermission(permission) ){
                throw new IllegalArgumentException("Invalid permission for collaborator: " + collaborator);
            }
        }
    }

    private boolean isValidPermission(String permission){

        return VALID_PERMISSIONS.contains(permission);
    }

    public boolean updateJournalEntry(ObjectId Id, JournalEntryResponse oldEntry, JournalEntry myEntry){

        oldEntry.setTitle( !myEntry.getTitle().isEmpty() ? myEntry.getTitle() : oldEntry.getTitle());
        oldEntry.setContent( ( myEntry.getContent() != null && !myEntry.getContent().isEmpty()) ? myEntry.getContent() : oldEntry.getContent() );

        List<String> newCollaborators = new ArrayList<>();
        HashMap<String, String> newPermissions = new HashMap<>();

        String creator = "";
        for( Map.Entry<String, String> entry : oldEntry.getPermissions().entrySet() ){

            if( entry.getValue().equals("OWNER") ){
                creator = entry.getKey();
                newCollaborators.add(entry.getKey());
                newPermissions.put(entry.getKey(), entry.getValue());
            }
        }

        if( myEntry.getIsCollaborative() ){

            for( Map.Entry<String, String> entry : myEntry.getPermissions().entrySet() ){
                if( entry.getKey().equals(creator) ){
                    continue;
                }
                if( entry.getValue().equals("OWNER") ){
                    newCollaborators.add(entry.getKey());
                    String permission = oldEntry.getPermissions().containsKey(entry.getKey()) ? oldEntry.getPermissions().get(entry.getKey()) : "READ";
                    newPermissions.put(entry.getKey(), permission );
                } else {
                    newCollaborators.add(entry.getKey());
                    newPermissions.put(entry.getKey(), entry.getValue());
                }
            }

            for( String collaborator : myEntry.getCollaborators() ){

                if( !(newPermissions.containsKey(collaborator)) ){
                    newCollaborators.add(collaborator);
                    newPermissions.put(collaborator, "READ");
                }
            }

            List<User> validCollaborators = userService.getUsersByUsernames(newCollaborators);
            List<String> invalidCollaborators = new ArrayList<>();

            for( String collaborator : newCollaborators ){
                boolean isValid = false;
                for( User validCollaborator : validCollaborators ){
                    if( validCollaborator.getUserName().equals(collaborator) ){
                        isValid = true;
                        break;
                    }
                }

                if( !isValid ){
                    invalidCollaborators.add(collaborator);
                }
            }

            newCollaborators.removeAll(invalidCollaborators);

            for( String invalidCollaborator : invalidCollaborators ){
                newPermissions.remove(invalidCollaborator);
            }

            if( !invalidCollaborators.isEmpty() ){
                System.out.println("The following collaborators were not added because they don't exist: " + invalidCollaborators);
            }

            oldEntry.setCollaborators( !myEntry.getCollaborators().isEmpty() ? newCollaborators : oldEntry.getCollaborators() );
            oldEntry.setPermissions( !myEntry.getPermissions().isEmpty() ? newPermissions : oldEntry.getPermissions() );
        }

        UpdateResult updateResult = saveExistingJournalEntry(Id, oldEntry);
        if( updateResult.getMatchedCount() == 0 ){
            throw new IllegalArgumentException("No journal entry found with ID: " + Id);
        }
        return true;
    }

    public boolean makeJournalEntryCollaborative(ObjectId Id, JournalEntryResponse oldEntry, MakeJournalCollaborativeRequest request){

        oldEntry.setIsCollaborative(true);
        List<String> newCollaborators = new ArrayList<>();
        HashMap<String, String> newPermissions = new HashMap<>();

        String creator = "";
        for( Map.Entry<String, String> entry : oldEntry.getPermissions().entrySet() ){

            if( entry.getValue().equals("OWNER") ){
                creator = entry.getKey();
                newCollaborators.add(entry.getKey());
                newPermissions.put(entry.getKey(), entry.getValue());
            }
        }

        for( Map.Entry<String, String> entry : request.getPermissions().entrySet() ){
            if( entry.getKey().equals(creator) ){
                continue;
            }
            if( entry.getValue().equals("OWNER") ){
                newCollaborators.add(entry.getKey());
                newPermissions.put(entry.getKey(), "READ" );
            } else {
                newCollaborators.add(entry.getKey());
                newPermissions.put(entry.getKey(), entry.getValue());
            }
        }

        for( String collaborator : request.getCollaborators() ){

            if( !(newPermissions.containsKey(collaborator)) ){
                newCollaborators.add(collaborator);
                newPermissions.put(collaborator, "READ");
            }
        }

        List<User> validCollaborators = userService.getUsersByUsernames(newCollaborators);
        List<String> invalidCollaborators = new ArrayList<>();

        for( String collaborator : newCollaborators ){
            boolean isValid = false;
            for( User validCollaborator : validCollaborators ){
                if( validCollaborator.getUserName().equals(collaborator) ){
                    isValid = true;
                    break;
                }
            }

            if( !isValid ){
                invalidCollaborators.add(collaborator);
            }
        }

        newCollaborators.removeAll(invalidCollaborators);

        for( String invalidCollaborator : invalidCollaborators ){
            newPermissions.remove(invalidCollaborator);
        }

        if( !invalidCollaborators.isEmpty() ){
            System.out.println("The following collaborators were not added because they don't exist: " + invalidCollaborators);
        }

        oldEntry.setCollaborators( !request.getCollaborators().isEmpty() ? newCollaborators : oldEntry.getCollaborators() );
        oldEntry.setPermissions( !request.getPermissions().isEmpty() ? newPermissions : oldEntry.getPermissions() );

        UpdateResult updateResult = saveExistingJournalEntry(Id, oldEntry);
        if( updateResult.getMatchedCount() == 0 ){
            throw new IllegalArgumentException("No journal entry found with ID: " + Id);
        }
        return true;
    }

    public UpdateResult saveExistingJournalEntry(ObjectId Id, JournalEntryResponse response){

        JournalEntry journalEntry = journalMapper.mapJournalEntryResponseToJournalEntry(response);

        Query queryForUpdate = new Query();
        queryForUpdate.addCriteria(Criteria.where("_id").is(Id));

        Update update = new Update();

        update.set("title", journalEntry.getTitle())
                .set("content", journalEntry.getContent())
                .set("isCollaborative", journalEntry.getIsCollaborative())
                .set("collaborators", journalEntry.getCollaborators())
                .set("permissions", journalEntry.getPermissions());

        return mongoTemplate.updateFirst(queryForUpdate, update, JournalEntry.class);
    }

    public List<JournalEntry> getAllJournalEntries(){

        return journalEntryRepository.findAll();
    }

    public List<JournalEntryResponse> getAllJournalEntriesByUserName(String userName){

        Query queryForUser = new Query();
        queryForUser.addCriteria(Criteria.where("userName").is(userName));
        queryForUser.fields().exclude("journalEntries");
        User user = mongoTemplate.findOne(queryForUser, User.class);

        if( user == null ){
            throw new IllegalArgumentException("User not found for username: " + userName);
        }

        Query queryForJournals = new Query();
        queryForJournals.addCriteria(new Criteria().andOperator(
                Criteria.where("collaborators").is(user.getUserName()),
                Criteria.where("permissions." + user.getUserName()).in("OWNER", "READ", "WRITE")
        ));

        List<JournalEntry> journalEntriesFromDB = mongoTemplate.find(queryForJournals, JournalEntry.class);

        List<JournalEntryResponse> journalEntriesResponse = new ArrayList<>();

        if( !journalEntriesFromDB.isEmpty() ){

            for( JournalEntry journalEntry : journalEntriesFromDB ){
                journalEntriesResponse.add(journalMapper.mapJournalEntryToResponse(journalEntry));
            }
            return journalEntriesResponse;

        } else {
            return null;
        }

    }

    public JournalEntryResponse getJournalEntryById(ObjectId myId){

        Query queryForJournal = new Query();
        queryForJournal.addCriteria(Criteria.where("_id").is(myId));
        JournalEntry journalEntry = mongoTemplate.findOne(queryForJournal, JournalEntry.class);

        if( journalEntry == null ){
            throw new RuntimeException("Journal Entry Not Found");
        }
        return journalMapper.mapJournalEntryToResponse(journalEntry);
    }

    public List<JournalEntryResponse> searchJournalEntriesByTitle(String title, String userName){

        Query queryForSearch = new Query();
        queryForSearch.addCriteria(new Criteria().andOperator(
                Criteria.where("title").regex(".*" + title + ".*", "i"),
                Criteria.where("collaborators").is(userName)
        ));

        List<JournalEntry> journalEntries = mongoTemplate.find(queryForSearch, JournalEntry.class);
        List<JournalEntryResponse> journalEntriesResponse = new ArrayList<>();

        for( JournalEntry journalEntry : journalEntries ){
            journalEntriesResponse.add(journalMapper.mapJournalEntryToResponse(journalEntry));
        }
        return journalEntriesResponse;
    }

    public boolean deleteJournalEntryById(ObjectId myId, String myUserName){

        User user = userService.getUserByUserName(myUserName);
        JournalEntryResponse journalEntryResponse = getJournalEntryById(myId);

        if( user != null && journalEntryResponse != null ){

            List<JournalEntry> allEntries = user.getJournalEntries();
            for (int i = 0; i < allEntries.size(); i++) {

                if (allEntries.get(i).getId().equals(myId)) {

                    allEntries.remove(i);
                    userService.saveUser(user);
                    journalEntryRepository.deleteById(myId);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isUserCollaborator(ObjectId myId, String myUserName){

        JournalEntryResponse journalEntryResponse = getJournalEntryById(myId);

        return journalEntryResponse != null && journalEntryResponse.getCollaborators().contains(myUserName);
    }

    public boolean isUserCollaboratorWithValidPermission(ObjectId myId, String myUserName){

        JournalEntryResponse journalEntryResponse = getJournalEntryById(myId);

        if( journalEntryResponse == null ) return false;

        if( !journalEntryResponse.getCollaborators().contains(myUserName) ) return false;

        String permission = journalEntryResponse.getPermissions().get(myUserName);

        return permission != null && (permission.equals("OWNER") || permission.equals("WRITE"));
    }

    public boolean isCollaboratorTheOwner(ObjectId myId, String myUserName){

        JournalEntryResponse journalEntryResponse = getJournalEntryById(myId);

        if( journalEntryResponse == null ) return false;

        if( !journalEntryResponse.getCollaborators().contains(myUserName) ) return false;

        String permission = journalEntryResponse.getPermissions().get(myUserName);

        return permission != null && permission.equals("OWNER");
    }

}
