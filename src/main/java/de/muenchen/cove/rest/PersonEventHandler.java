/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.Person;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;


@Component
@RepositoryEventHandler(Person.class)
public class PersonEventHandler {
    @HandleBeforeSave
    @HandleBeforeCreate
    public void handlePersonSave(Person p) {
        // Person zur Index-Kategorie wechseln, wenn sie positiv getestet wurde
        if (p.getKategorie() == Kategorie.KP && hatPositiveProbe(p)) {
            p.setKategorie(Kategorie.I);
        }
    }

    /**
     * hatPositiveProbe prüft ob die Person ein positives Testergebnis hat
     * @param p Person die überprüft wird
     * @return true, wenn eine positive Probe gefunden wurde
     */
    private boolean hatPositiveProbe(Person p) {
        if (p.getProben() == null || p.getProben().isEmpty()) {
            return false;
        }
        
        return p.getProben().stream().anyMatch(
                probe -> probe.getErgebnis() == Ergebnis.P
        );
    }
}
