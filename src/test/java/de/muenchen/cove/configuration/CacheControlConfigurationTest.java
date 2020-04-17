/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.configuration;

import de.muenchen.cove.MicroServiceApplication;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static de.muenchen.cove.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.cove.TestConstants.SPRING_TEST_PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = { MicroServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:testexample;DB_CLOSE_ON_EXIT=FALSE",
                "tomcat.gracefulshutdown.pre-wait-seconds=0"})
@ActiveProfiles(profiles = {SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE})
public class CacheControlConfigurationTest {

    private static final String ENTITY_ENDPOINT_URL = "/kontakpersons";

    private static final String EXPECTED_CACHE_CONTROL_HEADER_VALUES = "no-cache, no-store, must-revalidate";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Ignore//TODO
    public void testForCacheControlHeadersForEntityEndpoint() {
        ResponseEntity<String> response = testRestTemplate.exchange(ENTITY_ENDPOINT_URL, HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL));
        assertEquals(EXPECTED_CACHE_CONTROL_HEADER_VALUES, response.getHeaders().getCacheControl());
    }

}
