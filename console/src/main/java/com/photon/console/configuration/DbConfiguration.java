package com.photon.console.configuration;

import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties({ ConsoleXaProps.class })
public class DbConfiguration {

//    @Primary
//    @Bean(name = "consoleDataSource")
//    public DataSource consoleDataSource(XADataSourceWrapper wrapper, ConsoleXaProps props) throws Exception {
//        PGXADataSource xa = new PGXADataSource();
//        xa.setUrl(props.url());
//        xa.setUser(props.username());
//        xa.setPassword(props.password());
//        return wrapper.wrapDataSource(xa);
//    }

    @Primary
    @Bean(name = "consoleDataSource")
    public DataSource consoleDataSource(ObjectProvider<XADataSourceWrapper> wrapperProvider,
                                        ConsoleXaProps props) throws Exception {

        PGXADataSource xa = new PGXADataSource();
        xa.setUrl(props.url());
        xa.setUser(props.username());
        xa.setPassword(props.password());

        XADataSourceWrapper wrapper = wrapperProvider.getIfAvailable();
        if (wrapper != null) {
            return wrapper.wrapDataSource(xa);
        }

        // fallback to plain non-XA DataSource (Hikari recommended)
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(props.url())
                .username(props.username())
                .password(props.password())
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }


    @Bean(name = "consoleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean consoleEmf(
            EntityManagerFactoryBuilder builder,
            @Qualifier("consoleDataSource") DataSource ds) {

        Map<String, Object> jpaProps = new HashMap<>();

        jpaProps.put("hibernate.transaction.jta.platform", "org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform");
        jpaProps.put("jakarta.persistence.transactionType", "JTA");
        jpaProps.put("hibernate.hbm2ddl.auto", "update");
        jpaProps.put("jakarta.persistence.schema-generation.database.action", "update");
        jpaProps.put("hibernate.show_sql", "true");
        jpaProps.put("hibernate.format_sql", "true");
        jpaProps.put("hibernate.use_sql_comments", "true");

        return builder
                .dataSource(new TransactionAwareDataSourceProxy(ds))
                .packages("com.photon.console.entity", "com.photon.console.locality.entity")
                .persistenceUnit("console")
                .jta(true)
                .properties(jpaProps)
                .build();
    }
}