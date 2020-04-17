/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;


/**
 * This filter logs the username for requests.
 */
@Component
@Order(1)
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Getter
    private static final String NAME_UNAUTHENTICATED_USER = "unauthenticated";

    private static final String TOKEN_USER_NAME = "user_name";

    private static final String REQUEST_LOGGING_MODE_ALL = "all";

    private static final String REQUEST_LOGGING_MODE_CHANGING = "changing";

    private static final List<String> CHANGING_METHODS = Arrays.asList("POST", "PUT", "PATCH", "DELETE");

    /**
     * The property or a zero length string if no property is available.
     */
    @Value("${security.logging.requests:}")
    private String requestLoggingMode;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOG.debug("Initializing filter: {}", this);
    }

    /**
     * The method logs the username extracted out of the {@link SecurityContext}.
     * Additionally to the username, the kind of HTTP-Request and the targeted URI is logged.
     *
     * {@inheritDoc}
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (checkForLogging(httpRequest)) {
            LOG.info("User {} executed {} on URI {}",
                    getUsername(),
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI()
            );
        }
        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        LOG.debug("Destructing filter: {}", this);
    }

    /**
     * The method checks if logging the username should be done.
     *
     * @param httpServletRequest The request to check for logging.
     * @return True if logging should be done otherwise false.
     */
    private boolean checkForLogging(HttpServletRequest httpServletRequest) {
        return requestLoggingMode.equals(REQUEST_LOGGING_MODE_ALL)
                || (requestLoggingMode.equals(REQUEST_LOGGING_MODE_CHANGING)
                    && CHANGING_METHODS.contains(httpServletRequest.getMethod()));
    }

    /**
     * The method extracts the username out of the {@link OAuth2Authentication}.
     *
     * @return The username or a placeholder if there is no {@link OAuth2Authentication} available.
     */
    private static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
            HashMap details = (HashMap) oauth2Authentication.getUserAuthentication().getDetails();
            return (String) details.get(TOKEN_USER_NAME);
        } else {
            return NAME_UNAUTHENTICATED_USER;
        }
    }

}
