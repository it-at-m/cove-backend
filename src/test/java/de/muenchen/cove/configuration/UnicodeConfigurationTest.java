/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.configuration;

import de.muenchen.cove.MicroServiceApplication;
import de.muenchen.cove.domain.Person;
import de.muenchen.cove.rest.PersonRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.UUID;

import static de.muenchen.cove.TestConstants.SPRING_TEST_PROFILE;
import static de.muenchen.cove.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.cove.TestConstants.TheEntityDto;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = { MicroServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:testexample;DB_CLOSE_ON_EXIT=FALSE",
                "tomcat.gracefulshutdown.pre-wait-seconds=0"})
@ActiveProfiles(profiles = {SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE})
public class UnicodeConfigurationTest {

    private static final String ENTITY_ENDPOINT_URL = "/theEntities";

    /**
     * Decomposed string:
     * String "Ä-é" represented with unicode letters "A◌̈-e◌́"
     */
    private static final String TEXT_ATTRIBUTE_DECOMPOSED = "\u0041\u0308-\u0065\u0301";

    /**
     * Composed string:
     * String "Ä-é" represented with unicode letters "Ä-é".
     */
    private static final String TEXT_ATTRIBUTE_COMPOSED = "\u00c4-\u00e9";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PersonRepository theEntityRepository;

    @Test
    @Ignore //TODOs fixien
    public void testForNfcNormalization() {
        // Persist entity with decomposed string.
        final TheEntityDto theEntityDto = new TheEntityDto();
        theEntityDto.setTextAttribute(TEXT_ATTRIBUTE_DECOMPOSED);
        assertEquals(TEXT_ATTRIBUTE_DECOMPOSED.length(), theEntityDto.getTextAttribute().length());
        final TheEntityDto response = testRestTemplate.postForEntity(URI.create(ENTITY_ENDPOINT_URL), theEntityDto, TheEntityDto.class).getBody();

        // Check whether response contains a composed string.
        assertEquals(TEXT_ATTRIBUTE_COMPOSED, response.getTextAttribute());
        assertEquals(TEXT_ATTRIBUTE_COMPOSED.length(), response.getTextAttribute().length());

        // Extract uuid from self link.
        final UUID uuid = UUID.fromString(StringUtils.substringAfterLast(response.getRequiredLink("self").getHref(), "/"));

        // Check persisted entity contains a composed string via JPA repository.
        final Person theEntity = theEntityRepository.findById(uuid).orElse(null);
//        assertEquals(TEXT_ATTRIBUTE_COMPOSED, theEntity.getTextAttribute()); //TODO
//        assertEquals(TEXT_ATTRIBUTE_COMPOSED.length(), theEntity.getTextAttribute().length()); //TODo
    }

}
