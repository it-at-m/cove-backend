/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.domain;

import lombok.Data;

/**
 * Auswertungsdaten um Fortschritt der täglichen Anrufe zu visualisieren
 *
 */
@Data
public class DailyCallBericht {
	private long dailyCallsTodo;
	private long dailyCallsTotal;
}
