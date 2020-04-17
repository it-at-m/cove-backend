/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;


/**
 * This class represents a TheEntity.
 * <p>
 * The entity's content will be loaded according to the reference variable.
 * </p>
 */
@Entity(name = "PERSON")
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Indexed
@EntityListeners(AuditingEntityListener.class)
public class Person extends BaseEntity {

    private static final long serialVersionUID = 1L;
    public static final String SORT_POSTFIX = "Sort";

    @Version
    @Column(name = "VERSION")
    private long version;

    // ========= //
    // Variables //
    // ========= //
    @Field(name = "name")
    @Field(name = "name" + SORT_POSTFIX, analyze = Analyze.NO, index = Index.NO)
    @SortableField(forField = "name" + SORT_POSTFIX)
    @Column(name="name", nullable=false, length=255)
    @NotNull
    @Size(max=255)
    private String name;

    @Field(name = "vorname")
    @Field(name = "vorname" + SORT_POSTFIX, analyze = Analyze.NO, index = Index.NO)
    @SortableField(forField = "vorname" + SORT_POSTFIX)
    @Column(name="vorname", nullable=false, length=255)
    @NotNull
    @Size(max=255)
    private String vorname;

    @Field(name = "vornameNachname")
    public String getVornameNachname() {
        return getVorname() + " " + getName();
    }

    @Field(analyze = Analyze.NO)
    @Column(name="kategorie", length=5)
    @Enumerated(EnumType.STRING)
    @SortableField
    private Kategorie kategorie;

    public void setKategorie(Kategorie kategorie) {
        // Kategorie wird von Kontaktperson auf Indexed gesetzt
        // Achtung: Wird wegen Sprint Data Rest nur beim `PATCH` und nicht beim `PUT` ausgeführt, da
        // die this.kategorie bei `PUT` null ist.
        if (this.kategorie == Kategorie.KP && kategorie == Kategorie.I) {
            this.setKPBis(ZonedDateTime.now());
        }
        this.kategorie = kategorie;
    }

    @Column(name="medEinrichtung", length=5)
    @Enumerated(EnumType.STRING)
    private MedEinrichtung medEinrichtung;

    @Column(name="standort", length=1024)
    @Size(max=255)
    private String standort;

    @Column(name="geburtsdatum")
    private LocalDate geburtsdatum;

    @Column(name="strasse", length=255)
    @Size(max=255)
    private String strasse;

    @Column(name="plz", length=5)
    @Size(min=5, max=5)
    private String plz;

    @Column(name="ort", length=255)
    @Size(max=255)
    private String ort;

    @Column(name="landkreis", length=255)
    @Size(max=255)
    private String landkreis;

    @Column(name="ortsteil", length=255)
    @Size(max=255)
    private String ortsteil;

    @Column(name="land", length=255)
    @Size(max=255)
    private String land;

    @Column(name="telefon", length=255)
    @Size(max=255)
    private String telefon;

    @Column(name="mobile", length=255)
    @Size(max=255)
    private String mobile;

    @Column(name="mail", length=255)
    @Size(max=255)
    private String mail;

    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name = "personid")
    private List<Kontakt> kontakte = new LinkedList<>();

    @Embedded()
    private Quarantaene quarantaene;

    @ElementCollection
    @CollectionTable(name="PROBE", joinColumns=@JoinColumn(name="PERSONID"))
    private List<Probe> proben = new LinkedList<>();

    @Column(name="arbeitsinfos", columnDefinition = "CLOB")
    private String arbeitsinfos;

    @Column(name="kommentare", columnDefinition = "CLOB")
    private String kommentare;

    @Column(name="haushalt", length=1024)
    @Size(max=1024)
    private String haushalt;

    @Column(name="telefonnotizen", columnDefinition = "CLOB")
    private String telefonnotizen;

    @Column(name="doku", columnDefinition = "CLOB")
    private String doku;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "KPBIS", columnDefinition = "TIMESTAMP")
    private ZonedDateTime KPBis;

    @Column(name = "LETZTERKONTAKT", columnDefinition = "TIMESTAMP")
    private ZonedDateTime letzterKontakt;

    @Column(name = "ERSTKONTAKT", columnDefinition = "TIMESTAMP")
    private ZonedDateTime erstKontaktErfolgtAm;

    @Column(name = "ENDTELEFONAT", columnDefinition = "TIMESTAMP")
    private ZonedDateTime endTelefonatErfolgtAm;

    @Column(name = "AKTUELLER_BEARBEITER")
    private String aktuellerBearbeiter;

    @PrePersist
    public void prePersist() {
        if (proben != null) {
            proben.forEach(p -> p.setCreatedDate(LocalDateTime.now()));
        }
    }
}
