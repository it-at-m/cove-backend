/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove;


import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.MedEinrichtung;
import de.muenchen.cove.domain.Person;
import de.muenchen.cove.domain.Probe;
import de.muenchen.cove.domain.Quarantaene;
import de.muenchen.cove.rest.PersonEventHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

public class ExampleData {
    final static PersonEventHandler personEventHandler = new PersonEventHandler();

    public static Person getPersonHans() {
        final Person hans = new Person();
        hans.setName("Test");
        hans.setVorname("Hans");
        hans.setCreatedDate(LocalDateTime.now());

        personEventHandler.handlePersonSave(hans);
        return hans;
    }

    public static Person getPersonMarianne() {
        final Person hans = new Person();
        hans.setName("Test");
        hans.setVorname("Marianne");
        hans.setCreatedDate(LocalDateTime.now());

        personEventHandler.handlePersonSave(hans);
        return hans;
    }

    public static Person createPerson(final Kategorie kategorie) {
    	final Person person = new Person();

    	person.setName(UUID.randomUUID().toString());
    	person.setVorname(UUID.randomUUID().toString());
    	person.setKategorie(kategorie);

        personEventHandler.handlePersonSave(person);
        return person;
    }

    public static Person createPersonMitProben(final Kategorie kategorie, Probe... proben) {
        final Person person = createPerson(kategorie);
		person.setProben(Arrays.asList(proben));

		personEventHandler.handlePersonSave(person);
		return person;
	}

	public static Person createPersonMitProben(final Kategorie kategorie, final Quarantaene quarantaene,
			Probe... proben) {
		return createPersonMitProben(kategorie, quarantaene, null, proben);
	}

	public static Person createPersonMitProben(final Kategorie kategorie, final Quarantaene quarantaene,
			final MedEinrichtung medEinrichtung, Probe... proben) {
		final Person person = createPerson(kategorie);
		person.setProben(Arrays.asList(proben));
		person.setQuarantaene(quarantaene);
		person.setMedEinrichtung(medEinrichtung);

		personEventHandler.handlePersonSave(person);
		return person;
	}

    public static Probe createProbe(final Ergebnis ergebnis) {
    	final Probe probe = new Probe();

    	probe.setErgebnis(ergebnis);
    	probe.setKommentar(UUID.randomUUID().toString());

		return probe;
    }

    public static Quarantaene createInQuarantaene() {
    	return createQuarantaene(LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
    }

    public static Quarantaene createQuarantaeneVergangen() {
    	return createQuarantaene(LocalDate.now().minusDays(7), LocalDate.now().minusDays(3));
    }

    public static Quarantaene createQuarantaene(final LocalDate start, final LocalDate ende) {
    	final Quarantaene quarantaene = new Quarantaene();

    	quarantaene.setStart(start);
    	quarantaene.setEnde(ende);

		return quarantaene;
    }
}
