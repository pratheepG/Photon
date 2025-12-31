package com.photon.console.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Provides an EntityManagerFactoryBuilder-friendly environment:
 * - a JpaVendorAdapter (Hibernate) if none exists
 * - an EntityManagerFactoryBuilder bean using Boot's JpaProperties
 */
@Configuration
public class JpaBuilderConfig {

    private final JpaProperties jpaProperties;

    public JpaBuilderConfig(JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        return adapter;
    }

    @Bean
    public org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            JpaVendorAdapter jpaVendorAdapter,
            org.springframework.beans.factory.ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {

        return new org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder(
                jpaVendorAdapter,
                jpaProperties.getProperties(),
                persistenceUnitManager.getIfAvailable());
    }
}