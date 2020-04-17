/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.Field;
import org.springframework.data.annotation.CreatedDate;

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
@Embeddable
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode
@NoArgsConstructor
public class Probe {
    
    @CreatedDate
    @Field
    @Column(name = "CREATED_DATE", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdDate;

    // ========= //
    // Variables //
    // ========= //

    @Column(name="kommentar", length=1024)
    @Size(max=1024)
    private String kommentar;

    @Column(name="datum")
    private LocalDate datum;

    @Column(name="ergebnis")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Ergebnis ergebnis;
}
