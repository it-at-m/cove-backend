/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.gracefulshutdown;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {ServletWebServerFactoryAutoConfiguration.class, GracefulShutdownHealthCheck.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GracefulShutdownHealthCheckTest {

    @Autowired
    private GracefulShutdownHealthCheck gracefulShutdownHealthCheck;

    private final Map<String, Object> expectedDetailMap = new LinkedHashMap<>();

    @Test
    public void health() {
        assertEquals(Status.UP, gracefulShutdownHealthCheck.health().getStatus());
        expectedDetailMap.put(GracefulShutdownHealthCheck.GRACEFULSHUTDOWN, GracefulShutdownHealthCheck.APPLICATION_UP_MESSAGE);
        assertEquals(expectedDetailMap, gracefulShutdownHealthCheck.health().getDetails());
        expectedDetailMap.clear();
    }

    @Test
    public void setReady() {
        gracefulShutdownHealthCheck.setStatusDown();
        assertEquals(Status.DOWN, gracefulShutdownHealthCheck.health().getStatus());
        expectedDetailMap.put(GracefulShutdownHealthCheck.GRACEFULSHUTDOWN, GracefulShutdownHealthCheck.APPLICATION_DOWN_MESSAGE);
        assertEquals(expectedDetailMap, gracefulShutdownHealthCheck.health().getDetails());
        expectedDetailMap.clear();

        gracefulShutdownHealthCheck.setStatusUp();
        assertEquals(Status.UP, gracefulShutdownHealthCheck.health().getStatus());
        expectedDetailMap.put(GracefulShutdownHealthCheck.GRACEFULSHUTDOWN, GracefulShutdownHealthCheck.APPLICATION_UP_MESSAGE);
        assertEquals(expectedDetailMap, gracefulShutdownHealthCheck.health().getDetails());
        expectedDetailMap.clear();
    }

}
