package com.loopers.interfaces.batch.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
public class BatchSchemaInitializer {

    private final DataSource dataSource;

    public BatchSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() throws SQLException {
        if (!tablesExist()) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
            populator.execute(dataSource);
            System.out.println("Batch tables created!");
        }
    }

    private boolean tablesExist() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "BATCH_JOB_INSTANCE", null)) {
            return rs.next();
        }
    }
}
