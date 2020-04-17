/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import de.muenchen.cove.ExampleData;
import de.muenchen.cove.MicroServiceApplication;
import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.Kontakt;
import de.muenchen.cove.domain.Person;
import de.muenchen.cove.domain.Probe;
import de.muenchen.cove.domain.Quarantaene;
import de.muenchen.cove.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static de.muenchen.cove.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.cove.TestConstants.SPRING_TEST_PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {MicroServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:cove;DB_CLOSE_ON_EXIT=FALSE",
                "tomcat.gracefulshutdown.pre-wait-seconds=0"})
@ActiveProfiles(profiles = {SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE})
@Slf4j
public class PersonRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PersonRepository repository;


    @Before
    public void setup() {
        repository.deleteAll();
    }

    @Test
    @Ignore//Releasefirst-Spaeter schön
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    public void testSaveUndFindById() {
        final Person person2Save = new Person();
        checkAllFieldsSet(person2Save);

        final Person savedPerson = repository.save(person2Save);
        assertNotNull(savedPerson.getId());
        savedPerson.getKontakte().stream().forEach(k -> assertNotNull(k.getId()));

        final Person personById = repository.findById(savedPerson.getId()).orElse(null);
        assertNotNull(personById);
        assertNotNull(personById.getId());
        personById.getKontakte().stream().forEach(k -> assertNotNull(k.getId()));

        final List<Kontakt> kontakte2Save = new LinkedList<>(person2Save.getKontakte());
        kontakte2Save.forEach(k -> k.setId(null));
        final List<Kontakt> savedKontakte = new LinkedList<>(savedPerson.getKontakte());
        savedKontakte.forEach(k -> k.setId(null));
        final List<Kontakt> kontakteById = new LinkedList<>(personById.getKontakte());
        kontakteById.forEach(k -> k.setId(null));
        savedPerson.setKontakte(null);
        savedPerson.setProben(null);
        person2Save.setKontakte(null);
        person2Save.setProben(null);
        personById.setKontakte(null);
        personById.setProben(null);

        //ID wird beim Speichern gesetzt daher fuer den Vergleich irrelevant
        person2Save.setId(null);
        savedPerson.setId(null);
        personById.setId(null);

        //Sind die gespeicherten Daten gleich mit den zu speichernden
        assertEquals(person2Save, savedPerson);
        assertNotSame(person2Save, savedPerson);
        assertEquals(person2Save, personById);

        //stimmen die Kontakte?
//        assertEquals(kontakte2Save, savedKontakte); //TODO Fix Later
//        assertEquals(kontakte2Save, kontakteById); //TODO Fix Later

        //Stimmen die Proben
        //TODO
    }

    public void testCreatedDateAutoGeneration() {
        final Person person2Save = new Person();
        person2Save.setVorname("vorname");
        person2Save.setName("Name");

        person2Save.setProben(new LinkedList<>());
        final Probe p1 = new Probe(); person2Save.getProben().add(p1);
        final Probe p2 = new Probe(); person2Save.getProben().add(p2);

        person2Save.setKontakte(new LinkedList<>());
        final Kontakt k1 = new Kontakt(); person2Save.getKontakte().add(k1);
        final Kontakt k2 = new Kontakt(); person2Save.getKontakte().add(k2);

        final Person savedPerson = repository.save(person2Save);

        assertNotNull("CreatedDate bei Person fehlt", savedPerson.getCreatedDate());
        savedPerson.getKontakte().forEach(k -> assertNotNull("CreatedDate bei Kontakt fehlt", k.getCreatedDate()));
        savedPerson.getProben().forEach(p -> assertNotNull("CreatedDate bei Probe fehlt", p.getCreatedDate()));
    }

    @Test
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    public void testCounts() {
        final Person p1 = createPerson(Kategorie.I, 2, Ergebnis.N, "OT1");
        final Person p2 = createPerson(Kategorie.I, 4, Ergebnis.A, "OT1");
        final Person p3 = createPerson(Kategorie.KP, 8, Ergebnis.N, "OT2");
        final Person p4 = createPerson(Kategorie.KP, 1, Ergebnis.P, "OT3");

        repository.saveAll(Arrays.asList(p1, p2, p3, p4));

        assertEquals(2l, repository.countByKategorie(Kategorie.I));
        assertEquals(2l, repository.countByKategorie(Kategorie.KP));
        assertEquals(0l, repository.countByKategorie(Kategorie.KPN));

        assertEquals(2l, repository.countByOrtsteil("OT1"));
        assertEquals(1l, repository.countByOrtsteil("OT2"));
        assertEquals(1l, repository.countByOrtsteil("OT3"));
        assertEquals(0l, repository.countByOrtsteil("Nimmerland"));

        assertEquals(4l, repository.countByProbenErgebnis(Ergebnis.A));
        assertEquals(10l, repository.countByProbenErgebnis(Ergebnis.N));
        assertEquals(1l, repository.countByProbenErgebnis(Ergebnis.P));
    }

    private Person createPerson(Kategorie kategorie, int anzahlProben, Ergebnis probenErgebnis, String ortsteil) {
        final Person person = new Person();

        person.setName("Name");
        person.setVorname("vorname");

        person.setKategorie(kategorie);

        person.setProben(new LinkedList<>());
        for (int i=0; i<anzahlProben; i++) {
            final Probe p = new Probe();
            p.setErgebnis(probenErgebnis);
            p.setCreatedDate(LocalDateTime.now());
            p.setKommentar(UUID.randomUUID().toString());
            person.getProben().add(p);
        }

        person.setOrtsteil(ortsteil);

        return person;
    }

    @Test
    public void testQurantaeneCount() {
        Person hans = ExampleData.getPersonHans();
        hans.setKategorie(Kategorie.I);
        Quarantaene q1 = new Quarantaene();
        q1.setEnde(LocalDate.now());
        hans.setQuarantaene(q1);
        repository.save(hans);

        Person marianne = ExampleData.getPersonMarianne();
        marianne.setKategorie(Kategorie.KP);
        Quarantaene q2 = new Quarantaene();
        q2.setEnde(LocalDate.now().minusDays(1));
        marianne.setQuarantaene(q2);
        repository.save(marianne);

        long countI = repository.countByKategorieAndInQuarantaene(Kategorie.I);
        assertEquals(1L, countI);

        long countKPN = repository.countByKategorieAndInQuarantaene(Kategorie.KP);
        assertEquals(0L, countKPN);
    }

    @Test
    public void readIndexPersonenOhneSachbearbeiter() {
        final Person p1 = ExampleData.createPerson(Kategorie.I);
        p1.setLetzterKontakt(ZonedDateTime.now().minusMinutes(1)); //+
        final Person p2 = ExampleData.createPerson(Kategorie.I);
        p2.setLetzterKontakt(ZonedDateTime.now().plusMinutes(1));
        final Person p3 = ExampleData.createPerson(Kategorie.I); //+
        final Person p4 = ExampleData.createPerson(Kategorie.I);
        p4.setAktuellerBearbeiter("Musterschueler");
        final Person p5 = ExampleData.createPerson(Kategorie.KP);
        final Person p6 = ExampleData.createPerson(Kategorie.KP);
        final Person p7 = ExampleData.createPerson(Kategorie.KP);
        p7.setLetzterKontakt(ZonedDateTime.now());
        final Person p8 = ExampleData.createPerson(Kategorie.KP);
        p8.setLetzterKontakt(ZonedDateTime.now().minusMinutes(1));
        final Person p9 = ExampleData.createPerson(Kategorie.KP);
        p9.setLetzterKontakt(ZonedDateTime.now().plusHours(1));
        final Person p10 = ExampleData.createPerson(Kategorie.I);
        p10.setLetzterKontakt(ZonedDateTime.now().minusMinutes(5)); //+
        final Person p11 = ExampleData.createPerson(Kategorie.I);
        p11.setLetzterKontakt(ZonedDateTime.now().minusMinutes(15)); //+

        final List<Person> savedPerson = new LinkedList<>();
        savedPerson.add(repository.save(p1));
        savedPerson.add(repository.save(p2));
        savedPerson.add(repository.save(p3));
        savedPerson.add(repository.save(p4));
        savedPerson.add(repository.save(p5));
        savedPerson.add(repository.save(p6));
        savedPerson.add(repository.save(p7));
        savedPerson.add(repository.save(p8));
        savedPerson.add(repository.save(p9));
        savedPerson.add(repository.save(p10));
        savedPerson.add(repository.save(p11));

        savedPerson.stream().forEach(p -> log.info("saved Person > {}", p));

        final Page<Person> result = repository.indexpersonenOhneBearbeiterNichtKontaktiert(PageRequest.of(0, Integer.MAX_VALUE));
        assertEquals(4, result.getTotalElements());
        result.stream().forEach(p -> {
            assertNull(p.getAktuellerBearbeiter());
            assertEquals(Kategorie.I, p.getKategorie());
            assertTrue(p.getLetzterKontakt() == null ||ZonedDateTime.now().compareTo(p.getLetzterKontakt()) > 0);
        });

        //Die letzte Person in der Liste hat letztenKontakt null
        assertEquals(savedPerson.get(10).getId(), result.getContent().get(0).getId());
        assertEquals(savedPerson.get(9).getId(), result.getContent().get(1).getId());
        assertEquals(savedPerson.get(0).getId(), result.getContent().get(2).getId());
        assertEquals(savedPerson.get(2).getId(), result.getContent().get(3).getId());
        assertNull(result.getContent().get((int) result.getTotalElements()-1).getLetzterKontakt());
    }

    @Test
    public void personenFuerEndgespraech_richtigeTrefferJeKategorie() {
        final Person p1 = ExampleData.createPerson(Kategorie.I);
        p1.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(5))); //+
        final Person p2 = ExampleData.createPerson(Kategorie.I); //- QEnde fehlt
        final Person p3 = ExampleData.createPerson(Kategorie.KP); //- QEnde fehlt
        p3.setEndTelefonatErfolgtAm(ZonedDateTime.now());
        final Person p4 = ExampleData.createPerson(Kategorie.KP); //- QEnde fehlt
        p4.setAktuellerBearbeiter("Bearbeiter");
        final Person p5 = ExampleData.createPerson(Kategorie.KP);
        p5.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(5))); //+
        final Person p6 = ExampleData.createPerson(Kategorie.KP);
        p6.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now())); //+
        final Person p7 = ExampleData.createPerson(Kategorie.KP);
        p7.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().minusDays(3))); //+
        final Person p8 = ExampleData.createPerson(Kategorie.KP);
        p8.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(3))); //+
        final Person p9 = ExampleData.createPerson(Kategorie.KP); //- QEnde fehlt
        final Person p10 = ExampleData.createPerson(Kategorie.KP); //- hat bearbeiter
        p10.setAktuellerBearbeiter("Bearbeiter2");
        p10.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(5)));

        //+Treffer
        final Person p11 = ExampleData.createPerson(Kategorie.KP);
        p11.setLetzterKontakt(ZonedDateTime.now().minusHours(1));
        p11.setQuarantaene(ExampleData.createInQuarantaene());
        //+Treffer
        final Person p12 = ExampleData.createPerson(Kategorie.I);
        p12.setLetzterKontakt(ZonedDateTime.now().minusHours(1));
        p12.setQuarantaene(ExampleData.createInQuarantaene());

        final Person p13 = ExampleData.createPerson(Kategorie.KP); //-letzter KOntakt in Zukunft
        p13.setLetzterKontakt(ZonedDateTime.now().plusHours(1));
        p13.setQuarantaene(ExampleData.createInQuarantaene());
        final Person p14 = ExampleData.createPerson(Kategorie.I);  //-letzter KOntakt in Zukunft
        p14.setLetzterKontakt(ZonedDateTime.now().plusHours(1));
        p14.setQuarantaene(ExampleData.createInQuarantaene());

        final Person p15 = ExampleData.createPerson(Kategorie.KPN); //- keine QEnde
        final Person p16 = ExampleData.createPerson(Kategorie.KPN);
        p16.setQuarantaene(ExampleData.createInQuarantaene()); // +
        final Person p17 = ExampleData.createPerson(Kategorie.KPN);
        p17.setQuarantaene(ExampleData.createInQuarantaene());
        p17.setLetzterKontakt(ZonedDateTime.now().minusHours(1)); //+
        final Person p18 = ExampleData.createPerson(Kategorie.KPN);
        p18.setQuarantaene(ExampleData.createInQuarantaene());
        p18.setAktuellerBearbeiter("Master Bob");

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);
        repository.save(p4);
        repository.save(p5);
        repository.save(p6);
        repository.save(p7);
        repository.save(p8);
        repository.save(p9);
        repository.save(p10);
        repository.save(p11);
        repository.save(p12);
        repository.save(p13);
        repository.save(p14);
        repository.save(p15);
        repository.save(p16);
        repository.save(p17);
        repository.save(p18);

        final Page<Person> resultI = repository.personenFuerEndgespraech(PageRequest.of(0, Integer.MAX_VALUE), Kategorie.I, LocalDate.now().minusYears(1));
        final Page<Person> resultKP = repository.personenFuerEndgespraech(PageRequest.of(0, Integer.MAX_VALUE), Kategorie.KP, LocalDate.now().minusYears(1));
        final Page<Person> resultKPN = repository.personenFuerEndgespraech(PageRequest.of(0, Integer.MAX_VALUE), Kategorie.KPN, LocalDate.now().minusYears(1));

        //Verify Kategorie I
        assertEquals(2, resultI.getTotalElements());
        resultI.stream().forEach(p -> {
            assertTrue(p.getAktuellerBearbeiter() == null || "".equals(p.getAktuellerBearbeiter()));
            assertNull(p.getEndTelefonatErfolgtAm());
            assertTrue(Kategorie.I.equals(p.getKategorie()));
            assertTrue(p.getLetzterKontakt() == null || ZonedDateTime.now().compareTo(p.getLetzterKontakt()) > 0);
        });
        //Sicherstellen das sortiert ist
        for (int i=0; i<resultI.getNumberOfElements()-1; i++) {
            assertTrue(resultI.getContent().get(i).getQuarantaene().getEnde().compareTo(resultI.getContent().get(i+1).getQuarantaene().getEnde()) <= 0);
        }

      //Verify Kategorie KP
        assertEquals(5, resultKP.getTotalElements());
        resultKP.stream().forEach(p -> {
            assertTrue(p.getAktuellerBearbeiter() == null || "".equals(p.getAktuellerBearbeiter()));
            assertNull(p.getEndTelefonatErfolgtAm());
            assertTrue(Kategorie.KP.equals(p.getKategorie()));
            assertTrue(p.getLetzterKontakt() == null || ZonedDateTime.now().compareTo(p.getLetzterKontakt()) > 0);
        });
        //Sicherstellen das sortiert ist
        for (int i=0; i<resultKP.getNumberOfElements()-1; i++) {
            assertTrue(resultKP.getContent().get(i).getQuarantaene().getEnde().compareTo(resultKP.getContent().get(i+1).getQuarantaene().getEnde()) <= 0);
        }

      //Verify Kategorie KPN
        assertEquals(2, resultKPN.getTotalElements());
        resultKPN.stream().forEach(p -> {
            assertTrue(p.getAktuellerBearbeiter() == null || "".equals(p.getAktuellerBearbeiter()));
            assertNull(p.getEndTelefonatErfolgtAm());
            assertTrue(Kategorie.KPN.equals(p.getKategorie()) || Kategorie.I.equals(p.getKategorie()));
            assertTrue(p.getLetzterKontakt() == null || ZonedDateTime.now().compareTo(p.getLetzterKontakt()) > 0);
        });
        //Sicherstellen das sortiert ist
        for (int i=0; i<resultKPN.getNumberOfElements()-1; i++) {
            assertTrue(resultKPN.getContent().get(i).getQuarantaene().getEnde().compareTo(resultKPN.getContent().get(i+1).getQuarantaene().getEnde()) <= 0);
        }

        //Testen dass bei Datum == null alles unabhängig vom Datum kommt
        final Page<Person> resultDatumNull = repository.personenFuerEndgespraech(PageRequest.of(0, Integer.MAX_VALUE), Kategorie.I, null);
        //Verify Kategorie I
        assertEquals(2, resultDatumNull.getTotalElements());
        resultI.stream().forEach(p -> {
            assertTrue(p.getAktuellerBearbeiter() == null || "".equals(p.getAktuellerBearbeiter()));
            assertNull(p.getEndTelefonatErfolgtAm());
            assertEquals(Kategorie.I, p.getKategorie());
            assertTrue(p.getLetzterKontakt() == null || ZonedDateTime.now().compareTo(p.getLetzterKontakt()) > 0);
        });
        //Sicherstellen das sortiert ist
        for (int i=0; i<resultDatumNull.getNumberOfElements()-1; i++) {
            assertTrue(resultDatumNull.getContent().get(i).getQuarantaene().getEnde().compareTo(resultDatumNull.getContent().get(i+1).getQuarantaene().getEnde()) <= 0);
        }
    }

    @Test
    public void personenFuerEndgespraech_FilterungByQEndeAbIstKorrekt() throws Exception {
    	final Person p1 = ExampleData.createPerson(Kategorie.I); // - QEnde vor Grenzwert
    	p1.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().minusDays(1)));
    	final Person p2 = ExampleData.createPerson(Kategorie.I);
    	p2.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(2))); //+
    	final Person p3 = ExampleData.createPerson(Kategorie.I);
    	p3.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(7))); //+
    	final Person p4 = ExampleData.createPerson(Kategorie.I);
    	p4.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().minusDays(2))); // - QEnde vor Grenzwert
    	final Person p5 = ExampleData.createPerson(Kategorie.I);
    	p5.setQuarantaene(ExampleData.createQuarantaene(null, LocalDate.now().plusDays(3))); //+
    	final Person p6 = ExampleData.createPerson(Kategorie.I); //- Kein QEnde

    	final List<Person> savedPersonen = new ArrayList<>();

    	savedPersonen.add(repository.save(p1));
    	savedPersonen.add(repository.save(p2));
    	savedPersonen.add(repository.save(p3));
    	savedPersonen.add(repository.save(p4));
    	savedPersonen.add(repository.save(p5));
    	savedPersonen.add(repository.save(p6));

    	final Page<Person> result = repository.personenFuerEndgespraech(PageRequest.of(0, Integer.MAX_VALUE), Kategorie.I, LocalDate.now());

    	assertEquals(3, result.getNumberOfElements());
    	assertEquals(1, result.getTotalPages());

    	assertEquals(savedPersonen.get(1).getId(), result.getContent().get(0).getId());
    	assertEquals(savedPersonen.get(4).getId(), result.getContent().get(1).getId());
    	assertEquals(savedPersonen.get(2).getId(), result.getContent().get(2).getId());
    }

    @Test
    public void test_getPersonsWhereBearbeiter() {
        final Person p1 = ExampleData.createPerson(Kategorie.I);
        p1.setLetzterKontakt(ZonedDateTime.now().minusMinutes(1));
        p1.setAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);
        final Person p2 = ExampleData.createPerson(Kategorie.KP);
        final Person p3 = ExampleData.createPerson(Kategorie.KP);

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);

        final List<Person> meinePersonen = repository.findByAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);

        assertEquals(1, meinePersonen.size());
        assertEquals(Kategorie.I, meinePersonen.get(0).getKategorie());
    }

    @Test
    public void test_getEndgespraecheFuerDatum() {
        // +1
        final Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen());
        p1.setLetzterKontakt(ZonedDateTime.now().minusMinutes(1));
        p1.setAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);
        // +1
        final Person p2 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen());
        // +0
        final Person p3 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen());
        p3.setEndTelefonatErfolgtAm(ZonedDateTime.now());
        // +0
        final Person p4 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene());

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);
        repository.save(p4);

        Page<Person> endgespraecheFuerDatum = repository.getEndgespraecheFuerDatum(LocalDate.now(), null);

        assertEquals(2, endgespraecheFuerDatum.getContent().size());
        assertTrue(endgespraecheFuerDatum.getContent().stream().anyMatch(person -> person.getAktuellerBearbeiter().equals(SecurityUtils.DEFAULT_USERNAME)));
        assertTrue(endgespraecheFuerDatum.getContent().stream().anyMatch(person -> person.getKategorie().equals(Kategorie.I)));
        assertTrue(endgespraecheFuerDatum.getContent().stream().anyMatch(person -> person.getKategorie().equals(Kategorie.KP)));
    }

    @Test
    public void test_countEndgespraecheFuerDatum() {
        // +1
        final Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen());
        p1.setLetzterKontakt(ZonedDateTime.now().minusMinutes(1));
        p1.setAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);
        // +1
        final Person p2 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen());
        // +0
        final Person p3 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen());
        p3.setEndTelefonatErfolgtAm(ZonedDateTime.now());
        // +0
        final Person p4 = ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene());

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);
        repository.save(p4);

        int count = repository.countEndgespraecheFuerDatum(LocalDate.now());

        assertEquals(2, count);
    }

    @Test
    public void test_countDailyTotal() throws Exception {
        final Person p1 = ExampleData.createPerson(Kategorie.I);
        final Person p2 = ExampleData.createPerson(Kategorie.I);
        p2.setEndTelefonatErfolgtAm(ZonedDateTime.now());
        final Person p3 = ExampleData.createPerson(Kategorie.KPN);
        final Person p4 = ExampleData.createPerson(Kategorie.KP);
        final Person p5 = ExampleData.createPerson(Kategorie.I);

        repository.save(p1);
        repository.save(p2);
        repository.save(p3);
        repository.save(p4);
        repository.save(p5);

        assertEquals(2, repository.countDailyTotal());
    }
}
