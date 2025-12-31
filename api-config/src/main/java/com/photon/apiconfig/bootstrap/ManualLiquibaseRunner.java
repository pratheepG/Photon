package com.photon.apiconfig.bootstrap;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
@Order(1)
public class ManualLiquibaseRunner implements CommandLineRunner {

    private final DataSource dataSource;

    @Value("${spring.liquibase.parameters.photonProfilesActive}")
    private String activeProfile;

    public ManualLiquibaseRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            log.info("✔️ Liquibase changes started!");

            try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                                                     new ClassLoaderResourceAccessor(),
                                                     database)) {
                liquibase.setChangeLogParameter("photonProfilesActive", activeProfile);
                liquibase.update(new Contexts(activeProfile), new LabelExpression());
                log.info("✔️ Liquibase changes applied successfully!");
            }
        } catch (LiquibaseException e) {
            log.error("❌ Liquibase failed: {}", e.getMessage());
            throw e;
        }
    }
}