package com.loopers;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Supplier;

public class DummyDataGenerator {

    public static void seedAll(Supplier<Connection> connectionSupplier) {
        try (Connection conn = connectionSupplier.get()) {
            conn.setAutoCommit(false);

            insertBrands(conn, 1000);
            insertMembers(conn, 1000);
            insertUserPoints(conn);
            insertProducts(conn, 100_000);
            insertProductMeta(conn);
            insertProductStock(conn);
            insertProductLikes(conn);

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("더미 데이터 삽입 실패", e);
        }
    }

    private static void insertBrands(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO brand (name, description, created_at, updated_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                pstmt.setString(1, "브랜드" + i);
                pstmt.setString(2, "브랜드" + i + " 설명");
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void insertMembers(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO member (login_id, name, email, birth, gender, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                pstmt.setString(1, "user" + i);
                pstmt.setString(2, "유저" + i);
                pstmt.setString(3, "user" + i + "@test.com");
                pstmt.setString(4, "2000-01-01");
                pstmt.setString(5, (i % 2 == 0) ? "F" : "M");
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void insertUserPoints(Connection conn) throws SQLException {
        String sql = "INSERT INTO user_point (user_id, balance) SELECT id, 100000 FROM member";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void insertProducts(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO product (brand_id, name, description, amount, status, sell_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Random random = new Random();
            for (int i = 1; i <= count; i++) {
                pstmt.setLong(1, (i % 1000) + 1);
                pstmt.setString(2, "상품" + i);
                pstmt.setString(3, "상품" + i + " 설명");
                pstmt.setLong(4, (i % 100 + 1) * 100);
                pstmt.setString(5, "ON_SALE");
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
                if (i % 1000 == 0) pstmt.executeBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void insertProductMeta(Connection conn) throws SQLException {
        String sql = "INSERT INTO product_meta (product_id, like_count, review_count, view_count) SELECT id, (id % 500), 0, 0 FROM product";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void insertProductStock(Connection conn) throws SQLException {
        String sql = "INSERT INTO product_stock (product_id, stock) SELECT id, 100 + (id % 100) FROM product";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void insertProductLikes(Connection conn) throws SQLException {
        String sql = "INSERT INTO product_like (user_id, product_id, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 100_000; i++) {
                int likes = (i % 10) + 1;
                for (int j = 1; j <= likes; j++) {
                    pstmt.setLong(1, j);
                    pstmt.setLong(2, i);
                    pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setNull(5, Types.TIMESTAMP);
                    pstmt.addBatch();
                }
                if (i % 500 == 0) pstmt.executeBatch();
            }
            pstmt.executeBatch();
        }
    }

}

