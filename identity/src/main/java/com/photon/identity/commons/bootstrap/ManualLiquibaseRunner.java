package com.photon.identity.commons.bootstrap;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.database.jvm.JdbcConnection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Order(1)
public class ManualLiquibaseRunner implements CommandLineRunner {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    public ManualLiquibaseRunner(DataSource dataSource, ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            System.out.println("✔️ Liquibase changes started!");

            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new SpringResourceAccessor(applicationContext),
                    database)) {

                liquibase.update();
                System.out.println("✔️ Liquibase changes applied successfully!");
            }

        } catch (LiquibaseException e) {
            e.printStackTrace();
            System.err.println("❌ Liquibase failed: " + e.getMessage());
            throw e;
        }
    }
}