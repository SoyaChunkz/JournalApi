package com.sammo.journalApp.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(){
        MongoClient mongoClient = MongoClients.create("mongodb+srv://sameer:sameer123@cluster0.bh4if.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoDatabase database = mongoClient.getDatabase("myJournalDB");
        return new MongoTemplate(mongoClient, database.getName());
    }
}
