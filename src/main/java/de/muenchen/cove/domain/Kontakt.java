/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * This class represents a TheEntity.
 * <p>
 * The entity's content will be loaded according to the reference variable.
 * </p>
 */
@Entity(name = "KONTAKT")
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Kontakt extends BaseEntity  {

    private static final long serialVersionUID = 1L;

    // ========= //
    // Variables //
    // ========= //

    @OneToOne
    @JoinColumn(name="kontaktmit", referencedColumnName="id")
    @JsonIgnoreProperties("kontakte")
    private Person kontakt;

    @Column(name="kommentar", length=1024)
    @Size(max=1024)
    private String kommentar;

    @Column(name="kontakttyp", length=5)
    @Enumerated(EnumType.STRING)
    private Kontakttyp kontakttyp;

    @Column(name="kontaktdatum", length=255)
    private LocalDate kontaktdatum;
}
