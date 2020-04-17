/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import de.muenchen.cove.ExampleData;
import de.muenchen.cove.MicroServiceApplication;
import de.muenchen.cove.domain.Bericht;
import de.muenchen.cove.domain.DailyCallBericht;
import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.MedEinrichtung;
import de.muenchen.cove.domain.Person;
import de.muenchen.cove.utils.SecurityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static de.muenchen.cove.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.cove.TestConstants.SPRING_TEST_PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {MicroServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:cove;DB_CLOSE_ON_EXIT=FALSE",
                "tomcat.gracefulshutdown.pre-wait-seconds=0"})
@ActiveProfiles(profiles = {SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE})
public class PersonControllerTest {

    @Autowired
    private PersonController controller;
    @Autowired
    private PersonRepository repo;
    @Before
    public void before() {
        SecurityContextHolder.clearContext();

        repo.deleteAll();

        Person hans = ExampleData.getPersonHans();
        hans.setKategorie(Kategorie.I);
        repo.save(hans);

        Person marianne = ExampleData.getPersonMarianne();
        marianne.setKategorie(Kategorie.KPN);
        repo.save(marianne);
    }


    @Test
    public void testSearch(){
        PageRequest page = PageRequest.of(0, 100);
        Page<Person> alleKategorieI = controller.search(page, "", "", false, Kategorie.I);
        assertEquals(1, alleKategorieI.getTotalElements());

        Page<Person> alleKategorieKP = controller.search(page, "", "", false, Kategorie.KP);
        assertEquals(0, alleKategorieKP.getTotalElements());

        Page<Person> hansQuery = controller.search(page, "hans", "", false, null);
        assertEquals(1, hansQuery.getTotalElements());

        Page<Person> hansFuzzy = controller.search(page, "mariannä", "", false, null);
        assertEquals(1, hansFuzzy.getTotalElements());

        Page<Person> hansFuzzyMitKategorieKPN = controller.search(page, "hanz", "", false, Kategorie.KPN);
        assertEquals(0,hansFuzzyMitKategorieKPN.getTotalElements());

        // Test Ticket cove-backend/#29 Suche Personen funktioniert nicht mit Vor- und Nachnamen
        Page<Person> hansFuzzyVorUndNachname = controller.search(page, "Hans Test", "", false, null);
        assertEquals(2, hansFuzzyVorUndNachname.getTotalElements());
        assertTrue(hansFuzzyVorUndNachname.get().anyMatch(p -> p.getVorname().equals("Hans")));

        // Test Ticket cove-backend/#22 Suche Personen verhält sich nicht nachvollziehbar
        final Person personGreene = ExampleData.getPersonHans();
        personGreene.setVorname("Mr.");
        personGreene.setName("Greene");
        repo.save(personGreene);

        Page<Person> greeneSearch = controller.search(page, "greene", "", false, null);
        Page<Person> greenSearch = controller.search(page, "green", "", false, null);
        assertEquals(1, greeneSearch.getTotalElements());
        assertEquals(1, greenSearch.getTotalElements());
    }

    @Test
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    public void testBericht() {
    	repo.deleteAll();

    	repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), MedEinrichtung.AH, ExampleData.createProbe(Ergebnis.A)));
    	repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), MedEinrichtung.AH, ExampleData.createProbe(Ergebnis.P)));
    	repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene(), MedEinrichtung.KITA, ExampleData.createProbe(Ergebnis.A)));
    	repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene(), MedEinrichtung.PR, ExampleData.createProbe(Ergebnis.N)));
        // Person wird sofort zu Kategorie.I, da sie eine KP mit positiver Probe ist
        repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen(), MedEinrichtung.SCHU, ExampleData.createProbe(Ergebnis.P)));
    	repo.save(ExampleData.createPersonMitProben(Kategorie.KPN, null, MedEinrichtung.SCHU, ExampleData.createProbe(Ergebnis.A)));
    	repo.save(ExampleData.createPersonMitProben(null, ExampleData.createProbe(Ergebnis.A)));

    	final Bericht bericht = controller.bericht();

    	assertEquals(3l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.I.toString()));
    	assertEquals(2l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.KP.toString()));
    	assertEquals(1l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.KPN.toString()));
    	assertEquals(1l, (long) bericht.getAnzahl().getKategorie().get(Bericht.BERICHTSALIAS_KATEGORIE_NULL));
    	assertEquals(7l, (long) bericht.getAnzahl().getKategorie().get(Bericht.BERICHTSASLIAS_SUMME));

    	assertEquals(4l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.A.toString()));
    	assertEquals(1l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.N.toString()));
    	assertEquals(2l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.P.toString()));
    	assertEquals(7l, (long) bericht.getAnzahl().getProbenergebnis().get(Bericht.BERICHTSASLIAS_SUMME));

    	assertEquals(1l, (long) bericht.getAnzahl().getInQuarantaene().get(Kategorie.I.toString()));
    	assertEquals(2l, (long) bericht.getAnzahl().getInQuarantaene().get(Kategorie.KP.toString()));
        assertEquals(1l, (long) bericht.getAnzahl().getKonversionen().get(Kategorie.I.toString()));

        assertEquals(2l, (long) bericht.getAnzahl().getEinrichtungen().get(MedEinrichtung.AH.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getEinrichtungen().get(MedEinrichtung.KH.toString()));
        assertEquals(1l, (long) bericht.getAnzahl().getEinrichtungen().get(MedEinrichtung.KITA.toString()));
        assertEquals(1l, (long) bericht.getAnzahl().getEinrichtungen().get(MedEinrichtung.PR.toString()));
        assertEquals(2l, (long) bericht.getAnzahl().getEinrichtungen().get(MedEinrichtung.SCHU.toString()));
        assertEquals(1l, (long) bericht.getAnzahl().getEinrichtungen().get(Bericht.BERICHTSALIAS_KEY_NULL));
        assertEquals(7l, (long) bericht.getAnzahl().getEinrichtungen().get(Bericht.BERICHTSASLIAS_SUMME));

    }

    @Test
    public void testBerichtAufLeeremRepoMoeglich() {
        repo.deleteAll();

        final Bericht bericht = controller.bericht();

        assertEquals(0l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.I.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.KP.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getKategorie().get(Kategorie.KPN.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getKategorie().get(Bericht.BERICHTSALIAS_KATEGORIE_NULL));
        assertEquals(0l, (long) bericht.getAnzahl().getKategorie().get(Bericht.BERICHTSASLIAS_SUMME));

        assertEquals(0l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.A.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.N.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getProbenergebnis().get(Ergebnis.P.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getProbenergebnis().get(Bericht.BERICHTSASLIAS_SUMME));

        assertEquals(0l, (long) bericht.getAnzahl().getInQuarantaene().get(Kategorie.I.toString()));
        assertEquals(0l, (long) bericht.getAnzahl().getInQuarantaene().get(Kategorie.KP.toString()));
    }


    @Test
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    public void test_getDailyCallStatistik() {
        repo.deleteAll();

        // Indexpersonen für TotalCalls: 5
        // Person mit Bearbeiter - dailyCallsTodo unverändert
        Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A));
        p1.setAktuellerBearbeiter("hans.mueller");
        repo.save(p1);
        // Person ohne Bearbeiter und zuletztKontaktiert - dailyCallsTodo++
        Person p2 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        repo.save(p2);
        // Person ohne Bearbeiter und zuletztKontaktiert === jetzt - dailyCallsTodo++
        Person p3 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        p3.setLetzterKontakt(ZonedDateTime.now());
        repo.save(p3);
        // Person ohne Bearbeiter und zuletztKontaktiert = jetzt + 55 Minuten - dailyCallsTodo++
        Person p4 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        p4.setLetzterKontakt(ZonedDateTime.now().plusMinutes(55));
        repo.save(p4);
        // Person ohne Bearbeiter und zuletztKontaktiert = jetzt + 2 Stunden - dailyCallsTodo unverändert
        Person p5 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        p5.setLetzterKontakt(ZonedDateTime.now().plusHours(2));
        repo.save(p5);

        // Personen ohne Indexpersonen werden gar nicht dazu gerechnet:
        repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A)));
        repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.N)));
        // Person wird sofort zur Indexed Person, durch die positive Probe
        repo.save(ExampleData.createPersonMitProben(Kategorie.KP, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P)));
        repo.save(ExampleData.createPersonMitProben(Kategorie.KPN, ExampleData.createProbe(Ergebnis.A)));
        repo.save(ExampleData.createPersonMitProben(null, ExampleData.createProbe(Ergebnis.A)));

        final DailyCallBericht dailyCallBericht = controller.getDailyCallStatistik();

        // +1 zu 5 total und 3 daily call wegen der KP Person die zu Index wird
        assertEquals(6, dailyCallBericht.getDailyCallsTotal());
        assertEquals(4, dailyCallBericht.getDailyCallsTodo());
    }

    @Test
    public void test_getDailyCallStatistik_AufLeeremRepoMoeglich() {
        repo.deleteAll();

        final DailyCallBericht dailyCallBericht = controller.getDailyCallStatistik();

        assertEquals(0, dailyCallBericht.getDailyCallsTotal());
        assertEquals(0, dailyCallBericht.getDailyCallsTodo());
    }

    @Test
    public void test_generateNextDailyCall_noUsername() {
        repo.deleteAll();

        ResponseEntity<Person> personResponseEntity = controller.generateNextDailyCall();
        assertEquals(personResponseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_generateNextDailyCall_emptyRepo() {
        SecurityUtils.runAsDefaultUserWithAllRoles();
        repo.deleteAll();

        ResponseEntity<Person> personResponseEntity = controller.generateNextDailyCall();
        assertEquals(HttpStatus.NO_CONTENT, personResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateNextDailyCall_checkBearbeiter() {
        SecurityUtils.runAsDefaultUserWithAllRoles();
        repo.deleteAll();

        repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A)));
        repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P)));

        ResponseEntity<Person> personResponseEntity = controller.generateNextDailyCall();

        assertEquals(HttpStatus.OK, personResponseEntity.getStatusCode());
        assertEquals(SecurityUtils.DEFAULT_USERNAME, Objects.requireNonNull(personResponseEntity.getBody()).getAktuellerBearbeiter());
    }

    @Test
    public void test_generateNextEndgespraechCall_noUsername() {
        repo.deleteAll();

        ResponseEntity<Person> personResponseEntity = controller.generateNextEndgespraechCall(Kategorie.I, LocalDate.now());
        assertEquals(HttpStatus.BAD_REQUEST, personResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateNextEndgespraechCall_emptyRepo() {
        repo.deleteAll();
        SecurityUtils.runAsDefaultUserWithAllRoles();

        ResponseEntity<Person> personResponseEntity = controller.generateNextEndgespraechCall(Kategorie.I, LocalDate.now());
        assertEquals(HttpStatus.NO_CONTENT, personResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateNextEndgespraechCall_checkBearbeiter() {
        repo.deleteAll();
        SecurityUtils.runAsDefaultUserWithAllRoles();

        repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A)));
        repo.save(ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P)));

        ResponseEntity<Person> personResponseEntity = controller.generateNextDailyCall();

        assertEquals(HttpStatus.OK, personResponseEntity.getStatusCode());
        assertEquals(SecurityUtils.DEFAULT_USERNAME, Objects.requireNonNull(personResponseEntity.getBody()).getAktuellerBearbeiter());
    }

    @Test
    public void test_getMeineZuBearbeitendenPersonen() {
        repo.deleteAll();
        SecurityUtils.runAsDefaultUserWithAllRoles();

        Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A));
        p1.setAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);
        repo.save(p1);
        Person p2 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        p2.setAktuellerBearbeiter(SecurityUtils.DEFAULT_USERNAME);
        repo.save(p2);
        Person p3 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        p3.setAktuellerBearbeiter("hans meier");
        repo.save(p3);

        ResponseEntity<List<Person>> meinePersonen = controller.getMeineZuBearbeitendenPersonen();

        assertEquals(HttpStatus.OK, meinePersonen.getStatusCode());
        assertEquals(2, Objects.requireNonNull(meinePersonen.getBody()).size());
        assertEquals(SecurityUtils.DEFAULT_USERNAME, Objects.requireNonNull(meinePersonen.getBody()).get(0).getAktuellerBearbeiter());
    }

    @Test
    public void test_generateEndgespraechAnruferListeFuerDatum_falscheAnzahl() {
        repo.deleteAll();
        ResponseEntity<List<Person>> listResponseEntity = controller.generateEndgespraechAnruferListeFuerDatum(0, null);
        assertEquals(HttpStatus.BAD_REQUEST, listResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateEndgespraechAnruferListeFuerDatum_emptyDateErlaubt() {
        repo.deleteAll();
        ResponseEntity<List<Person>> listResponseEntity = controller.generateEndgespraechAnruferListeFuerDatum(50, null);
        assertEquals(HttpStatus.NO_CONTENT, listResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateEndgespraechAnruferListeFuerDatum_emptyResult() {
        repo.deleteAll();
        ResponseEntity<List<Person>> listResponseEntity = controller.generateEndgespraechAnruferListeFuerDatum(50, LocalDate.of(1970, 01, 01));
        assertEquals(HttpStatus.NO_CONTENT, listResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateEndgespraechAnruferListeFuerDatum_anzahlEins() {
        repo.deleteAll();

        Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A));
        repo.save(p1);
        Person p2 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        repo.save(p2);
        Person p3 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        repo.save(p3);

        ResponseEntity<List<Person>> listResponseEntity = controller.generateEndgespraechAnruferListeFuerDatum(1, LocalDate.now());
        assertEquals(HttpStatus.OK, listResponseEntity.getStatusCode());
    }

    @Test
    public void test_generateEndgespraechAnruferListeFuerDatum_ok() {
        repo.deleteAll();

        Person p1 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createInQuarantaene(), ExampleData.createProbe(Ergebnis.A));
        repo.save(p1);
        Person p2 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        repo.save(p2);
        Person p3 = ExampleData.createPersonMitProben(Kategorie.I, ExampleData.createQuarantaeneVergangen(), ExampleData.createProbe(Ergebnis.P));
        repo.save(p3);

        ResponseEntity<List<Person>> listResponseEntity = controller.generateEndgespraechAnruferListeFuerDatum(50, LocalDate.now());

        assertEquals(HttpStatus.OK, listResponseEntity.getStatusCode());

        List<Person> body = listResponseEntity.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        body.forEach(person -> assertEquals(Kategorie.I, person.getKategorie()));
        body.forEach(person -> assertEquals(ZonedDateTime.of(LocalDate.now().atTime(LocalTime.MAX), ZoneId.of("UTC")), person.getLetzterKontakt()));
        body.forEach(person -> assertEquals(ZonedDateTime.of(LocalDate.now().atTime(LocalTime.MAX), ZoneId.of("UTC")), person.getEndTelefonatErfolgtAm()));
    }

}
