/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class StatistikBericht {
	private Map<String, Long> kategorie = new HashMap<>();
	private Map<String, Long> probenergebnis = new HashMap<>();
	private Map<String, Long> inQuarantaene =  new HashMap<>();
	private Map<String, Long> konversionen =  new HashMap<>();
	private Map<String, Long> einrichtungen = new HashMap<>();


}
