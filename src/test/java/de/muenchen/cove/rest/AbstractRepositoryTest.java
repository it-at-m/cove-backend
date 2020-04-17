/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepositoryTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRepositoryTest.class);

    protected void checkAllFieldsSet(final Object object2Check) {
        final Class<?> clazz = object2Check.getClass();
        
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                assertNotNull(field.getName() + " ist nicht gesetzt", field.get(object2Check));
            } catch (Exception e) {
                LOG.error("Fehler bei Feld {}", field.getName(), e);
            }
            }
        );
    }

}
