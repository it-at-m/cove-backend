/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import lombok.Data;

/**
 * Zusammenfassung aller für einen Bericht relevanten Daten
 *
 */
@Data
public class Bericht {
	public static final String BERICHTSALIAS_KATEGORIE_NULL = "nicht_gesetzt";
	/**
	 * Alias für Schlüssel im Bericht für alle Werte die keinem anderen Schlüssel zugeordnet sind
	 */
	public static final String BERICHTSALIAS_KEY_NULL = BERICHTSALIAS_KATEGORIE_NULL;
	public static final String BERICHTSASLIAS_SUMME = "gesamt";

	private StatistikBericht anzahl = new StatistikBericht();
}
