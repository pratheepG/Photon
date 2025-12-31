package com.photon.console.identity.configuration;

import com.atomikos.spring.AtomikosDataSourceBean;
import com.photon.console.configuration.ConsoleXaProps;
import com.photon.console.gateway.configuration.GatewayXaProps;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;


@Configuration
@EnableConfigurationProperties({ IdentityXaProps.class })
public class IdentityDbConfig {

//    @Bean(name = "identityDataSource")
//    public DataSource identityDataSource(XADataSourceWrapper wrapper, IdentityXaProps props) throws Exception {
//        PGXADataSource xa = new PGXADataSource();
//        xa.setUrl(props.url());
//        xa.setUser(props.username());
//        xa.setPassword(props.password());
//        return wrapper.wrapDataSource(xa);
//    }

    @Bean(name = "identityDataSource")
    public DataSource identityDataSource(IdentityXaProps props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(props.url());
        ds.setUsername(props.username());
        ds.setPassword(props.password());
        return ds;
    }

    @Bean(name = "identityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean identityEmf(EntityManagerFactoryBuilder builder, @Qualifier("identityDataSource") DataSource ds) {

        return builder
                .dataSource(new TransactionAwareDataSourceProxy(ds))
                .packages("com.photon.console.identity.entity")
                .persistenceUnit("identity")
                .jta(true)
                .properties(Map.of(
                        "hibernate.transaction.jta.platform", "org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform",
                        "jakarta.persistence.transactionType", "JTA"
                ))
                .build();
    }
}