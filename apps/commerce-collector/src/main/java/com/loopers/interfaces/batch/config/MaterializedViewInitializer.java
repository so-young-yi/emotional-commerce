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
public class MaterializedViewInitializer {

    private final DataSource dataSource;

    public MaterializedViewInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() throws SQLException {
        if (!viewsExist()) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/migration/V002__create_materialized_views.sql"));
            populator.execute(dataSource);
            System.out.println("Materialized views created!");
        }
    }

    private boolean viewsExist() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "mv_product_rank_weekly", null)) {
            if (rs.next()) {
                return true;
            }
        }

        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "mv_product_rank_monthly", null)) {
            return rs.next();
        }
    }
}
