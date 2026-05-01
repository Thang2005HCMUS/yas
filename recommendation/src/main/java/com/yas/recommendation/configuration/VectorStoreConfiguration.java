package com.yas.recommendation.configuration;

import lombok.AllArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * VectorStore configuration that provides VectorStore bean for Spring Boot 4 compatibility.
 * This replaces the excluded PgVectorStoreAutoConfiguration which references removed classes.
 * Remove this class and enable auto-config after upgrading Spring AI to a newer version that is compatible with Spring Boot 4.
 */
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
@AllArgsConstructor
public class VectorStoreConfiguration {

    private final VectorStoreProperties vectorStoreProperties;

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .dimensions(vectorStoreProperties.getDimensions())
            .distanceType(vectorStoreProperties.getDistanceType())
            .indexType(vectorStoreProperties.getIndexType())
            .initializeSchema(vectorStoreProperties.isInitializeSchema())
            .build();
    }
}