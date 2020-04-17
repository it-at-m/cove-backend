/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.configuration;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
public class SearchConfiguration {

    Logger logger = LoggerFactory.getLogger(SearchConfiguration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void startIndexing() throws InterruptedException {
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        logger.info("Starting the index for Lucene-Search");
        LocalDateTime start = LocalDateTime.now();
        fullTextEntityManager.createIndexer().startAndWait();
        LocalDateTime end = LocalDateTime.now();
        logger.info("Finished index for Lucene-Search (Total Duration: {} ms)", Duration.between(start, end).toMillis());
    }
}
