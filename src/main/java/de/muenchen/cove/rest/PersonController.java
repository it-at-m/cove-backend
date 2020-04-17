/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import de.muenchen.cove.domain.Bericht;
import de.muenchen.cove.domain.DailyCallBericht;
import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.MedEinrichtung;
import de.muenchen.cove.domain.Person;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.util.StreamUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.muenchen.cove.domain.Person.SORT_POSTFIX;

@BasePathAwareController
@RequestMapping(value = "/persons")
@RestController
public class PersonController {

    /**
	 * Die Kategorien die für den Bericht relevant sind beim Ermitteln der Anzahl an Personen in Quarantaene
	 */
	private static final Kategorie[] BERICHT_KATEGORIE_IN_QUARANTAENE = {Kategorie.I, Kategorie.KP};

    /**
     * Wenn beim versuch eine Person zum anrufen zu reservieren die Daten bereits geändert wurden,
     * versucht das Backend bis zu MAX_NUMBER_OF_RETRIES_TO_RESERVE_PERSON mal erneut eine person zu bekommen.
     */
    @Value("${cove.reservePerson.maxRetries:5}")
	private int MAX_NUMBER_OF_RETRIES_TO_RESERVE_PERSON;

	public static final Logger LOG = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    PersonRepository repo;

    @PersistenceContext
    EntityManager entityManager;

    @GetMapping(value = "/search", produces = "application/json")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
    public Page<Person> search(Pageable pageable, @RequestParam(value = "q") String q,  @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
                               @RequestParam(value = "reverse", required = false) boolean reverse,
                               @RequestParam(value = "kategorie", required = false) Kategorie kategorie) {
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        final QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Person.class).
        overridesForField("vornameNachname", "edgeNGram_search").get();

        final BooleanJunction filteredQuery = queryBuilder.bool();
        if (isValidQuery(q)) {
            Query query = queryBuilder
                    .keyword()
                    .fuzzy()
                    .withEditDistanceUpTo(1)
                    .onFields("vornameNachname")
                    .matching(q.toLowerCase())

                    .createQuery();
            filteredQuery.must(query);
        } else {
            filteredQuery.must(queryBuilder.all().createQuery());
        }


        if (kategorie != null) {
            filteredQuery.must(queryBuilder.keyword().onField("kategorie").matching(kategorie).createQuery());
        }

        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(filteredQuery.createQuery(), Person.class);

        //Sortierung, sodass beim Paging auf den weiteren Seiten keine Duplikate auftreten
        switch (sort) {
            case "vorname":
            case "name":
            case "kategorie":
                Sort sorting = new Sort(new SortField(sort + SORT_POSTFIX, SortField.Type.STRING, reverse), new SortField("name" + SORT_POSTFIX, SortField.Type.STRING));
                fullTextQuery.setSort(sorting);
                break;
            default:
                fullTextQuery.setSort(q == null || q.isEmpty() ? new Sort(new SortField("name" + SORT_POSTFIX, SortField.Type.STRING)) : Sort.RELEVANCE);
        }

        fullTextQuery.setMaxResults(pageable.getPageSize());
        fullTextQuery.setFirstResult(pageable.getPageSize()*pageable.getPageNumber());
            //noinspection unchecked
        return new PageImpl<>(fullTextQuery.getResultList(), pageable,fullTextQuery.getResultSize());
    }

    @GetMapping(value = "/bericht", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_CREATE_BERICHTE.name())")
    public Bericht bericht() {
    	final Bericht bericht = new Bericht();

    	//CountByKategorie
    	final Map<String, Long> countByKategorie = bericht.getAnzahl().getKategorie();
    	Arrays.stream(Kategorie.values()).forEach(k -> countByKategorie.put(k.toString(), repo.countByKategorie(k)));
    	countByKategorie.put(Bericht.BERICHTSALIAS_KATEGORIE_NULL, repo.countByKategorie(null));
    	countByKategorie.put(Bericht.BERICHTSASLIAS_SUMME,
    				countByKategorie.keySet().stream().map(key -> countByKategorie.get(key)).reduce(Long::sum).orElse(0l));

    	//CountByProbenErgebnis
    	Map<String, Long> countByProbenergebnis = bericht.getAnzahl().getProbenergebnis();
    	Arrays.stream(Ergebnis.values()).forEach(e -> countByProbenergebnis.put(e.toString(), repo.countByProbenErgebnis(e)));
    	countByProbenergebnis.put(Bericht.BERICHTSASLIAS_SUMME,
    			countByProbenergebnis.keySet().stream().map(key -> countByProbenergebnis.get(key)).reduce(Long::sum).orElse(0l));

    	//CountByQuarantaene
    	Arrays.stream(BERICHT_KATEGORIE_IN_QUARANTAENE).forEach(k -> bericht.getAnzahl().getInQuarantaene().put(k.toString(), repo.countByKategorieAndInQuarantaene(k)));

    	//CountByEinrichtungen
    	final Map<String, Long> countByEinrichtung = bericht.getAnzahl().getEinrichtungen();
    	Arrays.stream(MedEinrichtung.values()).forEach(e -> countByEinrichtung.put(e.toString(), repo.countByMedEinrichtung(e)));
    	countByEinrichtung.put(Bericht.BERICHTSALIAS_KEY_NULL, repo.countByMedEinrichtung(null));
    	countByEinrichtung.put(Bericht.BERICHTSASLIAS_SUMME,
    			countByEinrichtung.keySet().stream().map(countByEinrichtung::get).reduce(Long::sum).orElse(0l));


    	//CountByKonversionen (angesteckte Kontaktpersonen)
        final Map<String, Long> countByKonversionen = bericht.getAnzahl().getKonversionen();
        countByKonversionen.put(Kategorie.I.toString(), repo.countIndexByWechselVonKPZuKategorieIndex());
		return bericht;
    }

    @GetMapping(value = "/dailyCallStatistik", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_CREATE_BERICHTE.name())")
    public DailyCallBericht getDailyCallStatistik() {
        final DailyCallBericht dailyCallBericht = new DailyCallBericht();

        dailyCallBericht.setDailyCallsTodo(repo.countIndexpersonenOhneBearbeiterNichtKontaktiertBis(ZonedDateTime.now().plusHours(1)));
        dailyCallBericht.setDailyCallsTotal(repo.countDailyTotal());

        return dailyCallBericht;
    }

    @GetMapping(value = "/meineZuBearbeitendenPersonen", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
    public ResponseEntity<List<Person>> getMeineZuBearbeitendenPersonen() {
        Optional<String> currentUsername = getCurrentUsername();
        if(!currentUsername.isPresent()) {
            LOG.error("#generateNextDailyCall couldn't figure out current username to set as a Bearbeiter...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(repo.findByAktuellerBearbeiter(currentUsername.get()), HttpStatus.OK);
    }

    @GetMapping(value = "/generateEndgespraechAnruferListeFuerDatum", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
    public ResponseEntity<List<Person>> generateEndgespraechAnruferListeFuerDatum(
            @RequestParam(value = "anzahl", defaultValue = "50") int anzahl,
            @RequestParam(value = "datum", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {

        if(anzahl <= 0) {
            LOG.error("#generateEndgespraechAnruferListeFuerDatum Anzahl ist kleiner oder gleich 0.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(datum == null) {
            datum = LocalDate.now();
        }

        final ZonedDateTime kontaktZeitpunkt = ZonedDateTime.of(datum.atTime(LocalTime.MAX), ZoneId.of("UTC"));

        Page<Person> people = repo.getEndgespraecheFuerDatum(datum, PageRequest.of(0, anzahl));

        if(people.getContent().size() == 0) {
            LOG.warn("#generateEndgespraechAnruferListeFuerDatum Keine Endgespräche für Datum gefunden.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<Person> responseList = new ArrayList<>();
        List<Person> failedToReserveList = new ArrayList<>();

        StreamUtils.createStreamFromIterator(people.iterator())
                .forEach(person -> {
                    person.setLetzterKontakt(kontaktZeitpunkt);
                    person.setEndTelefonatErfolgtAm(kontaktZeitpunkt);
                    try {
                        person = repo.save(person);
                        responseList.add(person);
                    } catch(OptimisticLockException ole) {
                        LOG.warn("#generateEndgespraechAnruferListeFuerDatum got Locking Exception for Person...please retry.");
                        failedToReserveList.add(person);
                    }
                });

        if(responseList.size() == 0 && failedToReserveList.size() != 0) {
            LOG.error("#generateEndgespraechAnruferListeFuerDatum Keine Person konnte aufgrund von Locking reserviert werden");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if(failedToReserveList.size() != 0) {
            LOG.warn("#generateEndgespraechAnruferListeFuerDatum Folgende Personen konnten nicht reserviert werden: {}", failedToReserveList.stream().map(person -> person.getId().toString()).collect(Collectors.joining(",")));
        }

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping(value = "/generateNextDailyCall", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
    public ResponseEntity<Person> generateNextDailyCall() {
        Optional<String> currentUsername = getCurrentUsername();
        if(!currentUsername.isPresent()) {
            LOG.error("#generateNextDailyCall couldn't figure out current username to set as a Bearbeiter...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<Person> personResponseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        int tries = 0;
        boolean sicherungIntakt = true;

        while (sicherungIntakt) {
            Page<Person> people = repo.indexpersonenOhneBearbeiterNichtKontaktiert(PageRequest.of(0, 1));
            personResponseEntity = this.handleReservierePersonFuerGespraech(people, currentUsername.get());
            if(personResponseEntity.getStatusCode().equals(HttpStatus.PRECONDITION_FAILED)) {
                tries++;
                if(tries >= MAX_NUMBER_OF_RETRIES_TO_RESERVE_PERSON) {
                    sicherungIntakt = false;
                }
            } else {
                sicherungIntakt = false;
            }
        }

        return personResponseEntity;
    }

    @GetMapping(value = "/generateNextEndgespraechCall", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
    public ResponseEntity<Person> generateNextEndgespraechCall(@RequestParam("kategorie") Kategorie kategorie,
    		@RequestParam(value = "quarantaeneEndeAb") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate quarantaeneEndeAb) {
        Optional<String> currentUsername = getCurrentUsername();
        if(!currentUsername.isPresent()) {
            LOG.error("#generateNextEndgespraechCall couldn't figure out current username to set as a Bearbeiter...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<Person> personResponseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        int tries = 0;
        boolean sicherungIntakt = true;

        while (sicherungIntakt) {
            Page<Person> people = repo.personenFuerEndgespraech(PageRequest.of(0, 1), kategorie, quarantaeneEndeAb);
            personResponseEntity = this.handleReservierePersonFuerGespraech(people, currentUsername.get());
            if(personResponseEntity.getStatusCode().equals(HttpStatus.PRECONDITION_FAILED)) {
                tries++;
                if(tries >= MAX_NUMBER_OF_RETRIES_TO_RESERVE_PERSON) {
                    sicherungIntakt = false;
                }
            } else {
                sicherungIntakt = false;
            }
        }

        return personResponseEntity;
    }

    private ResponseEntity<Person> handleReservierePersonFuerGespraech(Page<Person> people, String currentUsername) {
        if(people.getContent().size() > 0) {
            Person person = people.getContent().get(0);
            person.setAktuellerBearbeiter(currentUsername);
            try {
                person = repo.save(person);
                return new ResponseEntity<Person>(person, HttpStatus.OK);
            } catch(OptimisticLockException ole) {
                LOG.error("#handleReservierePersonFuerGespraech got Locking Exception for Person...please retry.");
                return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
            }
        } else {
            LOG.error("#handleReservierePersonFuerGespraech Keine nächste Person gefunden.");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private boolean isValidQuery(String q) {
        return q != null && q.length() > 2;
    }

    private Optional<String> getCurrentUsername() {
        Optional<String> username = Optional.empty();

        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                username = Optional.ofNullable(((UserDetails) principal).getUsername());
            } else {
                username = Optional.ofNullable(principal.toString());
            }
        } catch(Exception e) {
            LOG.error("#getCurrentUsername exception occured trying to get current username:", e);
        }

        return username;
    }

}
