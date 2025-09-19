-- 주간 랭킹 Materialized View
CREATE TABLE IF NOT EXISTS mv_product_rank_weekly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    week_start_date DATE NOT NULL,
    ranking_score DECIMAL(15, 4) NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    total_order_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_weekly_product_week (product_id, week_start_date),
    INDEX idx_weekly_week_score (week_start_date, ranking_score DESC),
    INDEX idx_weekly_product (product_id),
    INDEX idx_weekly_created (created_at)
) ENGINE=InnoDB COMMENT='주간 상품 랭킹 집계 테이블';

-- 월간 랭킹 Materialized View
CREATE TABLE IF NOT EXISTS mv_product_rank_monthly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    month_start_date DATE NOT NULL,
    ranking_score DECIMAL(15, 4) NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    total_order_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_monthly_product_month (product_id, month_start_date),
    INDEX idx_monthly_month_score (month_start_date, ranking_score DESC),
    INDEX idx_monthly_product (product_id),
    INDEX idx_monthly_created (created_at)
) ENGINE=InnoDB COMMENT='월간 상품 랭킹 집계 테이블';

-- 일간 상품 메트릭스 테이블 (배치 소스 데이터)
CREATE TABLE IF NOT EXISTS product_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    date DATE NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    total_order_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_product_date (product_id, date),
    INDEX idx_date (date),
    INDEX idx_product (product_id)
) ENGINE=InnoDB COMMENT='일간 상품 메트릭스 집계 테이블';