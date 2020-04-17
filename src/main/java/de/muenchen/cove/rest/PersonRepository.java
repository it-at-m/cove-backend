/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.rest;

import de.muenchen.cove.domain.Ergebnis;
import de.muenchen.cove.domain.Kategorie;
import de.muenchen.cove.domain.MedEinrichtung;
import de.muenchen.cove.domain.Person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;


/**
 * Provides a Repository for {@link Person}. This Repository is exported as a REST resource.
 * <p>
 * The Repository handles CRUD Operations. Every Operation is secured and takes care of the tenancy.
 * For specific Documentation on how the generated REST point behaves, please consider the Spring Data Rest Reference
 * <a href="http://docs.spring.io/spring-data/rest/docs/current/reference/html/">here</a>.
 * </p>
 */
@RepositoryRestResource
@PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_READ_PERSONEN.name())")
public interface PersonRepository extends PagingAndSortingRepository<Person, UUID> { //NOSONAR

    /**
     * Name for the specific cache.
     */
    String CACHE = "PERSON_CACHE";

    /**
     * Get one specific {@link Person} by its unique id.
     *
     * @param id The identifier of the {@link Person}.
     * @return The {@link Person} with the requested id.
     */
    @Override
    Optional<Person> findById(UUID id);

    /**
     * Create or update a {@link Person}.
     * <p>
     * If the id already exists, the {@link Person} will be overridden, hence update.
     * If the id does not already exist, a new {@link Person} will be created, hence create.
     * </p>
     *
     * @param theEntity The {@link Person} that will be saved.
     * @return the saved {@link Person}.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    <S extends Person> S save(S theEntity);

    /**
     * Create or update a collection of {@link Person}.
     * <p>
     * If the id already exists, the {@link Person}s will be overridden, hence update.
     * If the id does not already exist, the new {@link Person}s will be created, hence create.
     * </p>
     *
     * @param entities The {@link Person} that will be saved.
     * @return the collection saved {@link Person}.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    <S extends Person> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * Delete the {@link Person} by a specified id.
     *
     * @param id the unique id of the {@link Person} that will be deleted.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    void deleteById(UUID id);

    /**
     * Delete a {@link Person} by entity.
     *
     * @param entity The {@link Person} that will be deleted.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    void delete(Person entity);

    /**
     * Delete multiple {@link Person} entities by their id.
     *
     * @param entities The Iterable of {@link Person} that will be deleted.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    void deleteAll(Iterable<? extends Person> entities);

    /**
     * Delete all {@link Person} entities.
     */
    @Override
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_WRITE_PERSONEN.name())")
    void deleteAll();

    long countByKategorie(@Param("kategorie") Kategorie kategorie);

    long countByProbenErgebnis(@Param("ergebnis") Ergebnis ergebnis);

    long countByOrtsteil(@Param("ortsteil") String ortsteil);

    long countByMedEinrichtung(@Param("einrichtung") MedEinrichtung einrichtung);

    @Query("SELECT count(*) from de.muenchen.cove.domain.Person p where p.kategorie = ?1 and ( p.quarantaene.ende >= CURRENT_DATE or p.quarantaene.ende is null)")
    @PreAuthorize("hasAuthority(T(de.muenchen.cove.security.AuthoritiesEnum).COVE_BACKEND_CREATE_BERICHTE.name())")
    long countByKategorieAndInQuarantaene(@Param("kategorie") Kategorie kategorie1);

    @Query("SELECT count(*) from de.muenchen.cove.domain.Person p where p.kategorie = 'I' and p.KPBis is not null")
    long countIndexByWechselVonKPZuKategorieIndex();

    List<Person> findByKontakte_kontakt_id(@Param("id") UUID id);

    @Query("SELECT p FROM de.muenchen.cove.domain.Person p where p.kategorie = 'I' and (p.aktuellerBearbeiter is null or p.aktuellerBearbeiter = '') and (p.letzterKontakt < CURRENT_TIMESTAMP or p.letzterKontakt is null) and p.endTelefonatErfolgtAm is null order by p.letzterKontakt asc NULLS LAST")
    Page<Person> indexpersonenOhneBearbeiterNichtKontaktiert(Pageable pageable);

    @Query("SELECT count(*) FROM de.muenchen.cove.domain.Person p where p.kategorie = 'I' and (p.aktuellerBearbeiter is null or p.aktuellerBearbeiter = '') and p.endTelefonatErfolgtAm is null and (p.letzterKontakt < ?1 or p.letzterKontakt is null)")
    int countIndexpersonenOhneBearbeiterNichtKontaktiertBis(@Param("nichtKontaktiertBis") ZonedDateTime nichtKontaktiertBis);

    @Query("SELECT p FROM de.muenchen.cove.domain.Person p "
    		+ "where ( p.kategorie = ?1) and p.endTelefonatErfolgtAm is null "
    			+ "and (p.aktuellerBearbeiter is null or p.aktuellerBearbeiter = '') and (p.letzterKontakt <= CURRENT_TIMESTAMP or p.letzterKontakt is NULL) "
    			+ "and ( (p.quarantaene.ende >= ?2 or ?2 is NULL) AND p.quarantaene.ende is not NULL ) "
    		+ "ORDER BY p.quarantaene.ende ASC")
    Page<Person> personenFuerEndgespraech(Pageable pageable, @Param("kategorie") Kategorie kategorie, @Param("quarantaeneEndeAb")@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate quarantaeneEndeAb);

    List<Person> findByAktuellerBearbeiter (@Param("bearbeiter") String bearbeiter);

    @Query("SELECT p FROM de.muenchen.cove.domain.Person p where ( p.kategorie = 'KP' or p.kategorie = 'I' ) and p.endTelefonatErfolgtAm is null and p.quarantaene.ende <= :datum")
    Page<Person> getEndgespraecheFuerDatum(@Param("datum")@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum, Pageable pageable);

    @Query("SELECT count(*) FROM de.muenchen.cove.domain.Person p where ( p.kategorie = 'KP' or p.kategorie = 'I' ) and p.endTelefonatErfolgtAm is null and p.quarantaene.ende <= :datum")
    int countEndgespraecheFuerDatum(@Param("datum")@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum);

    @Query("SELECT count(*) FROM de.muenchen.cove.domain.Person p WHERE (p.kategorie = 'I' AND p.endTelefonatErfolgtAm is null)")
    long countDailyTotal();
}
