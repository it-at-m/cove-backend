/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@Configuration
public class SpringRestConfiguration implements RepositoryRestConfigurer {

    /**
     * The method returns a {@link Validator} to get correct validation error messages.
     *
     * @return A {@link LocalValidatorFactoryBean} to get correct validation error messages.
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * See {@link RepositoryRestConfigurer#configureRepositoryRestConfiguration}
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.DEFAULT);
    }

    /**
     * See {@link RepositoryRestConfigurer#configureValidatingRepositoryEventListener}
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("afterCreate", validator());
        validatingListener.addValidator("beforeCreate", validator());
        validatingListener.addValidator("afterSave", validator());
        validatingListener.addValidator("beforeSave", validator());
        validatingListener.addValidator("beforeLinkSave", validator());
        validatingListener.addValidator("afterLinkSave", validator());
    }

}
