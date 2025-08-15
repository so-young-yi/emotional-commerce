# ERD

---

- 모든 테이블은 `created_at`, `updated_at`, `deleted_at`(소프트삭제) 필드를 공통적으로 가집니다.  
  (ERD 복잡도 감소를 위해 아래 정의에는 생략)
- 모든 FK는 논리적 관계만 나타내며, 실제 FK 제약조건은 적용하지 않습니다.  
  (실무상 삭제/변경의 유연성을 위해 FK 제약을 두지 않음)

```mermaid
erDiagram
   
    USER {
        bigint id PK
        varchar name
    }

    USER_POINT {
        bigint id PK
        bigint ref_user_id FK
        int balance
    }

    BRAND {
        bigint id PK
        varchar name
    }

    PRODUCT {
        bigint id PK
        bigint ref_brand_id FK
        varchar name
        varchar description
        int price
        int quantity
        varchar status
    }

    PRODUCT_LIKE {
        bigint id PK
        bigint ref_user_id FK
        bigint ref_product_id FK
    }

    "ORDER" {
        bigint id PK
        bigint ref_user_id FK
        datetime created_at
        varchar status
    }

    ORDER_ITEM {
        bigint id PK
        bigint ref_order_id FK
        bigint ref_product_id FK
        int quantity
        int price_snapshot
        varchar product_name_snapshot
    }

    PAYMENT {
        bigint id PK
        bigint ref_order_id FK
        int amount
        datetime paid_at
        varchar status
    }

    POINT_HISTORY {
        bigint id PK
        bigint ref_user_point_id FK
        int amount
        varchar reason
        datetime created_at
    }

    %% 관계 정의
    USER ||--|| USER_POINT : "유저 포인트"
    USER ||--o{ PRODUCT_LIKE : "좋아요한 상품"
    USER ||--o{ "ORDER" : "주문"

    USER_POINT ||--o{ POINT_HISTORY : "포인트 변동 기록"

    BRAND ||--o{ PRODUCT : "상품 소유"

    PRODUCT ||--o{ PRODUCT_LIKE : "좋아요"
    PRODUCT ||--|| ORDER_ITEM : "주문상품"

    "ORDER" ||--o{ ORDER_ITEM : "주문 내역"
    "ORDER" ||--|| PAYMENT : "결제"

    ORDER_ITEM }o--|| PRODUCT : "상품 참조"

    PAYMENT }o--|| "ORDER" : "주문 참조"

    %% 모든 테이블은 created_at, updated_at, deleted_at(소프트삭제) 필드를 공통적으로 가집니다.
    %% (ERD 복잡도 감소를 위해 위 정의에는 생략)
    %% 모든 FK는 논리적 관계만 나타내며, 실제 FK 제약조건은 적용하지 않음.
    %% (실무상 삭제/변경의 유연성을 위해 FK 제약을 두지 않음)
```