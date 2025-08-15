

# 유저 플로우 (플로우 차트)

---

```mermaid

flowchart TD
    A[로그인] --> B[브랜드 목록 조회]
    B --> C[브랜드별 상품 목록 조회]
    C --> D[상품 상세 정보 확인]
    D --> E[상품 좋아요/좋아요 해제]
    E --> F[좋아요한 상품 목록 조회]
    F --> G[여러 상품 선택 및 수량 입력 후 주문 요청]
    G --> H{주문 검증}
    H -- 상품 미존재 --> I1[404 Not Found 반환]
    H -- 판매중 아님 --> I2[409 Conflict 반환]
    H -- 수량 10개 초과 --> I3[400 Bad Request 반환]
    H -- 통과 --> J[주문서 생성 및 스냅샷 저장]
    J --> K[포인트로 결제]
    K --> L{결제 검증}
    L -- 포인트 부족 --> M1[400 Bad Request 반환]
    L -- 미로그인 --> M2[401 Unauthorized 반환]
    L -- 통과 --> N[결제 성공]
    N --> O[주문상품 재고 차감]
    O --> P{재고 차감 성공?}
    P -- 예 --> Q[주문 확정 및 포인트 차감/내역 기록]
    P -- 아니오 --> R[결제 취소 및 안내]
    Q --> S[종료]
    R --> S
    I1 --> S
    I2 --> S
    I3 --> S
    M1 --> S
    M2 --> S


```





# 주요 유저 플로우 - 시퀀스 다이어그램

---

## 1. 브랜드 및 상품 탐색 플로우

1. 사용자는 로그인한다.

2. 브랜드 목록을 조회한다.

   - 실패: 인증 헤더 없음 → 401 Unauthorized

3. 브랜드를 선택한다.

   - 실패: 브랜드 미존재 → 404 Not Found

4. 선택한 브랜드의 상품 목록을 조회한다.

5. 관심 있는 상품을 선택해 상세 정보를 확인한다.

   - 실패: 상품 미존재 → 404 Not Found


6. 사용자는 상품 상세 페이지에서 좋아요를 누른다.

   - 실패: 미로그인 → 401 Unauthorized

7. 이미 좋아요를 누른 상품이면 좋아요가 해제된다.


```mermaid
sequenceDiagram
    participant User
    participant BrandController
    participant BrandService
    participant ProductController
    participant ProductService
    participant ProductLikeController
    participant ProductLikeService
    participant ProductLikeRepository

    %% 1. 브랜드 목록 조회 (X-USER-ID로 인증)
    User->>BrandController: 브랜드 목록 조회 요청 [GET /api/v1/brands, X-USER-ID 포함]
    BrandController->>BrandService: X-USER-ID로 사용자 인증 및 브랜드 목록 조회
    alt 인증 실패 (X-USER-ID 없음/유효하지 않음)
        BrandService-->>BrandController: 401 Unauthorized
        BrandController-->>User: 401 Unauthorized
    else 인증 성공
        BrandService-->>BrandController: 브랜드 목록 반환
        BrandController-->>User: 브랜드 목록 반환

        User->>BrandController: 브랜드 선택
        alt 브랜드 미존재
            BrandController-->>User: 404 Not Found (브랜드 없음)
        else 브랜드 존재
            BrandController-->>User: 브랜드 선택 성공

            User->>ProductController: 해당 브랜드의 상품 목록 조회 [GET /api/v1/products?brand=브랜드, X-USER-ID 포함]
            ProductController->>ProductService: 브랜드 및 사용자 인증 후 상품 목록 조회
            ProductService-->>ProductController: 상품 목록 반환
            ProductController-->>User: 상품 목록 반환

            User->>ProductController: 상품 상세 정보 확인 [GET /api/v1/products/{productId}, X-USER-ID 포함]
            ProductController->>ProductService: 상품 상세 정보 조회
            alt 상품 미존재
                ProductService-->>ProductController: 404 Not Found (상품 없음)
                ProductController-->>User: 404 Not Found (상품 없음)
            else 상품 존재
                ProductService-->>ProductController: 상품 상세 정보 반환
                ProductController-->>User: 상품 상세 정보 반환

                User->>ProductLikeController: 상품 좋아요/해제 요청 [POST/DELETE /api/v1/products/{productId}/likes, X-USER-ID 포함]
                ProductLikeController->>ProductLikeService: X-USER-ID로 사용자 인증 및 좋아요/해제 처리
                alt 미로그인(X-USER-ID 없음/유효하지 않음)
                    ProductLikeService-->>ProductLikeController: 401 Unauthorized
                    ProductLikeController-->>User: 401 Unauthorized
                else 로그인 상태
                    ProductLikeService->>ProductLikeRepository: 좋아요 존재 여부 확인
                    alt 좋아요 미존재
                        ProductLikeService->>ProductLikeRepository: 좋아요 저장
                        ProductLikeRepository-->>ProductLikeService: 저장 완료
                        ProductLikeService-->>ProductLikeController: 좋아요 등록 성공
                        ProductLikeController-->>User: 좋아요 등록 성공
                    else 이미 좋아요함
                        ProductLikeService->>ProductLikeRepository: 좋아요 삭제
                        ProductLikeRepository-->>ProductLikeService: 삭제 완료
                        ProductLikeService-->>ProductLikeController: 좋아요 해제 성공
                        ProductLikeController-->>User: 좋아요 해제 성공
                    end
                end
            end
        end
    end
```


## 2. 좋아요 상품 주문 플로우

1. 사용자는 “좋아요한 상품 목록”을 조회한다.

2. 주문할 상품(복수 선택 가능)을 선택한다.

3. 각 상품별로 수량을 선택한다.

   - 실패: 상품 미존재 → 404 Not Found

   - 실패: 주문 수량이 재고 초과 → 409 Conflict

   - 실패: 상품이 판매중 상태가 아님 → 409 Conflict

4. 주문서를 생성한다.

   - 주문 시점의 상품 정보(이름, 가격, 수량 등)는 스냅샷으로 저장됨.

5. 결제 화면으로 이동한다.

6. 포인트로 주문 전체 금액을 결제한다.

   - 실패: 포인트 부족 → 400 Bad Request

   - 실패: 미로그인 → 401 Unauthorized

7. 결제 성공 시, 각 상품별 재고를 차감한다.

   - 실패: 결제 성공 후 재고 부족 확인 → 결제 취소(환불) 및 안내, 409 Conflict 또는 500 Internal Server Error

8. 결제 및 주문 내역, 포인트 사용 내역이 기록된다.


```mermaid
sequenceDiagram

  participant User

  participant ProductLikeController

  participant ProductLikeService

  participant ProductLikeRepository

  participant OrderController

  participant OrderService

  participant ProductService

  participant ProductRepository

  participant PaymentService

  participant PointService



  %% 1. 좋아요한 상품 목록 조회

  User->>ProductLikeController: GET /api/v1/users/{userId}/likes (좋아요한 상품 목록 요청)

  ProductLikeController->>ProductLikeService: 좋아요 상품 목록 가져오기

  ProductLikeService->>ProductLikeRepository: 유저의 좋아요 상품 조회

  ProductLikeRepository-->>ProductLikeService: 좋아요 목록 반환

  ProductLikeService-->>ProductLikeController: 상품 목록 반환

  ProductLikeController-->>User: 상품 목록 반환



  %% 2~4. 주문 요청 및 생성

  User->>OrderController: POST /api/v1/orders (주문 요청: 상품/수량 목록)

  OrderController->>OrderService: 주문 생성 요청

  loop 각 주문 상품별

    OrderService->>ProductRepository: 상품 존재 확인

    ProductRepository-->>OrderService: 상품 반환

    alt 상품 미존재

      OrderService-->>OrderController: 404 Not Found

      OrderController-->>User: 404 Not Found

    else

      OrderService->>ProductService: 판매 가능/수량 가능 여부 확인

      alt 재고 부족

        ProductService-->>OrderService: 409 Conflict

        OrderService-->>OrderController: 409 Conflict

        OrderController-->>User: 409 Conflict

      else 판매중/재고 충분

        ProductService-->>OrderService: 확인 완료

      end

    end

  end

  OrderService->>OrderService: 주문서 생성 및 상품 스냅샷 저장

  OrderService-->>OrderController: 주문서 반환

  OrderController-->>User: 주문서 반환



  %% 5~8. 결제 및 포인트/재고 차감

  User->>OrderController: 결제 요청 (주문, 포인트)

  OrderController->>PaymentService: 결제 처리 요청

  PaymentService->>PointService: 포인트 차감 요청

  alt 포인트 부족

    PointService-->>PaymentService: 400 Bad Request

    PaymentService-->>OrderController: 400 Bad Request

    OrderController-->>User: 400 Bad Request

  else

    PointService-->>PaymentService: 차감 성공

    loop 각 주문 상품별

      PaymentService->>ProductRepository: 상품 조회

      ProductRepository-->>PaymentService: 상품 반환

      PaymentService->>ProductService: 재고 차감 시도

      alt 재고 차감 실패

        ProductService-->>PaymentService: 차감 실패

        PaymentService->>PointService: 포인트 환불 요청

        PointService-->>PaymentService: 환불 완료

        PaymentService-->>OrderController: 결제 취소(환불), 안내

        OrderController-->>User: 결제 취소(환불), 안내

      else 재고 차감 성공

        ProductService-->>PaymentService: 차감 성공

      end

    end

    PaymentService-->>OrderController: 결제 성공

    OrderController-->>User: 결제 성공, 주문/포인트 내역 반환

  end
```