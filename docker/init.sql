-- 기존 테이블 삭제
DROP TABLE IF EXISTS product_like;
DROP TABLE IF EXISTS user_coupon;
DROP TABLE IF EXISTS orders_item;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS point_history;
DROP TABLE IF EXISTS product_meta;
DROP TABLE IF EXISTS product_stock;
DROP TABLE IF EXISTS user_point;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS brand;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS product;

-- 테이블 생성
CREATE TABLE brand (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(1000),
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL,
                       deleted_at DATETIME(6)
) ENGINE=InnoDB;

CREATE TABLE member (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        login_id VARCHAR(255),
                        name VARCHAR(255),
                        email VARCHAR(255),
                        birth VARCHAR(255),
                        gender ENUM ('F','M'),
                        created_at DATETIME(6) NOT NULL,
                        updated_at DATETIME(6) NOT NULL,
                        deleted_at DATETIME(6)
) ENGINE=InnoDB;

CREATE TABLE user_point (
                            user_id BIGINT NOT NULL PRIMARY KEY,
                            balance BIGINT NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES member(id)
) ENGINE=InnoDB;

CREATE TABLE product (
                         id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         brand_id BIGINT,
                         name VARCHAR(255),
                         description VARCHAR(255),
                         amount BIGINT,
                         status ENUM ('ON_SALE','SOLD_OUT','STOPPED'),
                         sell_at DATETIME(6) NOT NULL,
                         created_at DATETIME(6) NOT NULL,
                         updated_at DATETIME(6) NOT NULL,
                         deleted_at DATETIME(6),
                         FOREIGN KEY (brand_id) REFERENCES brand(id)
) ENGINE=InnoDB;

CREATE TABLE product_meta (
                              product_id BIGINT NOT NULL PRIMARY KEY,
                              like_count BIGINT,
                              review_count BIGINT,
                              view_count BIGINT,
                              FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

CREATE TABLE product_stock (
                               product_id BIGINT NOT NULL PRIMARY KEY,
                               stock BIGINT,
                               FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

CREATE TABLE product_like (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              product_id BIGINT NOT NULL,
                              created_at DATETIME(6) NOT NULL,
                              updated_at DATETIME(6) NOT NULL,
                              deleted_at DATETIME(6),
                              UNIQUE KEY UK_user_product (user_id, product_id),
                              FOREIGN KEY (user_id) REFERENCES member(id),
                              FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

-- (이하 orders, orders_item, payment, point_history, coupon, user_coupon 등도 필요시 추가)

-- ---------------------------
-- 데이터 대량 insert
-- ---------------------------

-- 브랜드 1,000개
INSERT INTO brand (id, name, description, created_at, updated_at)
SELECT seq, CONCAT('브랜드', seq), CONCAT('브랜드', seq, ' 설명'), NOW(6), NOW(6)
FROM (
         SELECT @row := @row + 1 AS seq
         FROM information_schema.columns c1, information_schema.columns c2,
             (SELECT @row := 0) AS init
             LIMIT 1000
     ) t;

-- 유저 1,000명
INSERT INTO member (id, login_id, name, email, birth, gender, created_at, updated_at)
SELECT seq, CONCAT('user', seq), CONCAT('유저', seq), CONCAT('user', seq, '@test.com'), '2000-01-01', 'F', NOW(6), NOW(6)
FROM (
         SELECT @rownum := @rownum + 1 AS seq
         FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
             UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
             (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
             UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
             (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
             UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t3,
             (SELECT @rownum := 0) t0
             LIMIT 1000
     ) x;

-- 유저 포인트 1,000명
INSERT INTO user_point (user_id, balance)
SELECT id, 100000 FROM member;

-- 상품 100,000개
INSERT INTO product (id, brand_id, name, description, amount, status, sell_at, created_at, updated_at)
SELECT seq, (seq % 1000) + 1, CONCAT('상품', seq), CONCAT('상품', seq, ' 설명'),
       (seq % 100 + 1) * 100, 'ON_SALE', NOW(6), NOW(6), NOW(6)
FROM (
         SELECT @row2 := @row2 + 1 AS seq
         FROM information_schema.columns c1, information_schema.columns c2, (SELECT @row2 := 0) AS init
             LIMIT 100000
     ) t;

-- 상품 메타 100,000개
INSERT INTO product_meta (product_id, like_count, review_count, view_count)
SELECT id, (id % 500), 0, 0 FROM product;

-- 상품 재고 100,000개
INSERT INTO product_stock (product_id, stock)
SELECT id, 100 + (id % 100) FROM product;

-- 상품 좋아요 (각 상품마다 1~10명 유저가 좋아요)
DELIMITER $$
CREATE PROCEDURE insert_likes()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE j INT;
  WHILE i <= 100000 DO
    SET j = 1;
    WHILE j <= (i % 10) + 1 DO
      INSERT INTO product_like (user_id, product_id, created_at, updated_at, deleted_at)
      VALUES (j, i, NOW(6), NOW(6), NULL);
      SET j = j + 1;
END WHILE;
    SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

CALL insert_likes();
DROP PROCEDURE insert_likes;
