package com.sammo.journalApp.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoProperties mongoProperties){

        MongoClient mongoClient = MongoClients.create(mongoProperties.getUri());
        MongoDatabase database = mongoClient.getDatabase(mongoProperties.getDatabase());

        return new MongoTemplate(mongoClient, database.getName());
    }
}
