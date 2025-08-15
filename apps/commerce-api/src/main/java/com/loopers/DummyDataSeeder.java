package com.loopers;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;

@Profile("local")
@Component
public class DummyDataSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("DummyDataSeeder.run");
        DummyDataGenerator.seedAll(() -> {
            String url = "jdbc:mysql://localhost:3306/loopers?rewriteBatchedStatements=true";
            String user = "application";
            String pass = "application";
            try {
                return DriverManager.getConnection(url, user, pass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
