/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.utils;

import de.muenchen.cove.security.AuthoritiesEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import java.util.Arrays;

public class SecurityUtils {

    public static final String DEFAULT_USERNAME = "max.mustermann";

    /**
     * Configures the Spring Security {@link SecurityContext} to be authenticated as the user with the given username and
     * password as well as the given granted authorities.
     *
     * @param username must not be {@literal null} or empty.
     * @param password must not be {@literal null} or empty.
     * @param roles
     */
    public static void runAs(String username, String password, String[] roles) {

        Assert.notNull(username, "Username must not be null!");
        Assert.notNull(password, "Password must not be null!");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, password, AuthorityUtils.createAuthorityList(roles)));
    }

    public static void runAsDefaultUserWithAllRoles() {
        runAs(
                DEFAULT_USERNAME,
                "CorrectHorseBatteryStaple",
                Arrays.stream(AuthoritiesEnum.values()).map(Enum::name).toArray(String[]::new));
    }


}
