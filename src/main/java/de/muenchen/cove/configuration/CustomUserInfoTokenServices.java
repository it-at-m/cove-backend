/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.oauth2.provider.OAuth2Authentication;


/**
 * This class extends the class {@link UserInfoTokenServices} by the caching functionality for the method
 * {@link CustomUserInfoTokenServices#loadAuthentication(String)}.
 *
 * The configuration for the cache is done in class {@link CachingConfiguration}.
 *
 * If the annotation {@link EnableCaching} is not present within the application,
 * the caching functionality is not available. The above mentioned annotation is defined
 * in class {@link CachingConfiguration}.
 */
public class CustomUserInfoTokenServices extends UserInfoTokenServices {

    private static final Logger LOG = LoggerFactory.getLogger(CustomUserInfoTokenServices.class);

    public static final String NAME_AUTHENTICATION_CACHE = "authentication_cache";

    public CustomUserInfoTokenServices(String userInfoEndpointUrl, String clientId) {
        super(userInfoEndpointUrl, clientId);
    }

    /**
     * The method is caching the authentication using the access token given in the parameter as a key.
     *
     * @param accessToken The access token.
     * @return The {@link OAuth2Authentication} according the access token given in the parameter.
     */
    @Override
    @Cacheable(NAME_AUTHENTICATION_CACHE)
    public OAuth2Authentication loadAuthentication(String accessToken) {
        LOG.debug("Loading and caching OAuth2Authentication");
        return super.loadAuthentication(accessToken);
    }

}
