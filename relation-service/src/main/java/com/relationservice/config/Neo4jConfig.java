package com.relationservice.config;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.TransactionManager;

@Configuration
public class Neo4jConfig {
    @Bean
    @Primary // Ép Spring ưu tiên dùng cái này khi có xung đột
    public TransactionManager transactionManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }
}