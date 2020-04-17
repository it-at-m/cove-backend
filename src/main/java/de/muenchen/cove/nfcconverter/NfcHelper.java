/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.nfcconverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Hilfsklasse für das NFC-Normalisieren</p>
 *
 * @see Normalizer
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NfcHelper {

    private static final Logger LOG = LoggerFactory.getLogger(NfcHelper.class);

    /**
     * Konvertieren eines String in die kanonische Unicode-Normalform (NFC)
     *
     * @param in Eingabe-String
     * @return Normalisierter String.
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String nfcConverter(final String in) {
        if (in == null) {
            LOG.debug("String BEFORE nfc conversion is \"null\".");
            return null;
        }

        LOG.debug("String BEFORE nfc conversion: \"{}\".", in);
        LOG.debug("Length of String BEFORE nfc conversion: {}.", in.length());
        final String nfcConvertedContent = Normalizer.normalize(in, Normalizer.Form.NFC);
        LOG.debug("String AFTER nfc conversion: \"{}\".", nfcConvertedContent);
        LOG.debug("Length of String AFTER nfc conversion: {}.", nfcConvertedContent.length());
        return nfcConvertedContent;
    }

    /**
     * Konvertieren eines {@link StringBuffer}-Inhalts in die kanonische Unicode-Normalform (NFC)
     *
     * @param in Eingabe
     * @return Normalisierter Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static StringBuffer nfcConverter(final StringBuffer in) {
        return new StringBuffer(nfcConverter(in.toString()));
    }

    /**
     * Konvertieren eines Array von Strings in die kanonische Unicode-Normalform (NFC)
     *
     * @param original Eingabe-Array
     * @return Array mit normalisierten Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String[] nfcConverter(final String[] original) {
        return Arrays.stream(original)
                .map(NfcHelper::nfcConverter)
                .toArray(String[]::new);
    }

    /**
     * Konvertieren einer {@link Map} von Strings in die kanonische Unicode-Normalform (NFC).
     *
     * @param original Eingabe-Map
     * @return Map mit normalisierten Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Map<String, String[]> nfcConverter(final Map<String, String[]> original) {
        final HashMap<String, String[]> nfcConverted = new HashMap<>(original.size());
        original.forEach((nfdKey, nfdValueArray) -> nfcConverted.put(
                nfcConverter(nfdKey),
                nfcConverter(nfdValueArray))
        );
        return nfcConverted;
    }

    /**
     * Konvertieren eines {@link Cookie}s in die kanonische Unicode-Normalform (NFC).
     *
     * @param original Cookie
     * @return Cookie mit normalisierten Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Cookie nfcConverter(Cookie original) {
        final Cookie nfcCookie = new Cookie(NfcHelper.nfcConverter(original.getName()), NfcHelper.nfcConverter(original.getValue()));
        nfcCookie.setComment(NfcHelper.nfcConverter(original.getComment()));
        if (original.getDomain() != null) {
            nfcCookie.setDomain(NfcHelper.nfcConverter(original.getDomain()));
        }
        nfcCookie.setPath(NfcHelper.nfcConverter(original.getPath()));
        return nfcCookie;
    }

    /**
     * Konvertieren eines Arrays von {@link Cookie}s in die kanonische Unicode-Normalform (NFC).
     *
     * @param original Cookies
     * @return Cookies mit normalisierten Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Cookie[] nfcConverter(final Cookie[] original) {
        if (original == null) {
            return null;
        }
        return Arrays.stream(original)
                .map(NfcHelper::nfcConverter)
                .toArray(Cookie[]::new);
    }

    /**
     * Konvertieren der Header eines {@link HttpServletRequest} von Strings in die kanonische Unicode-Normalform (NFC).
     *
     * @param originalRequest Der {@link HttpServletRequest} zur Extraktion und Konvertierung der Header.
     * @return Map mit normalisierten Inhalt.
     * @see #nfcConverter(String)
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static Map<String, List<String>> nfcConverterForHeadersFromOriginalRequest(final HttpServletRequest originalRequest) {
        final Map<String, List<String>> converted = new CaseInsensitiveMap<>();
        Collections.list(originalRequest.getHeaderNames()).forEach(nfdHeaderName -> {
            final String nfcHeaderName = NfcHelper.nfcConverter(nfdHeaderName);
            final List<String> nfcHeaderEntries = Collections.list(originalRequest.getHeaders(nfdHeaderName)).stream()
                    .map(NfcHelper::nfcConverter)
                    .collect(Collectors.toList());
            converted.put(nfcHeaderName, nfcHeaderEntries);
        });
        return converted;
    }

}
