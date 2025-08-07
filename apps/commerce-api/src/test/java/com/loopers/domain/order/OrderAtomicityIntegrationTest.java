package com.loopers.domain.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("주문 원자성/실패 케이스 통합 테스트")
class OrderAtomicityIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductMetaRepository productMetaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;

    private Long productId;
    private Long userId;

    @BeforeEach
    void setUp() {
        // 유저, 포인트, 상품, 상품메타 세팅
        UserModel user = userRepository.save(new UserModel("user", "유저", "user@email.com", "2000-01-01", Gender.F));
        userId = user.getId();
        userPointRepository.save(new UserPointModel(userId, 10_000L));
        ProductModel product = productRepository.save(new ProductModel(null, 1L, "상품", "설명", new Money(10_000L), ProductStatus.ON_SALE, ZonedDateTime.now()));
        productId = product.getId();
        productMetaRepository.save(ProductMetaModel.builder().productId(productId).stock(10L).likeCount(0L).reviewCount(0L).viewCount(0L).build());
    }

    @Test
    @DisplayName("재고 부족 시 전체 롤백")
    void shouldRollback_whenStockNotEnough() {
        OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                Collections.singletonList(new OrderV1Dto.OrderItem(productId, 20L)) // 재고 10, 주문 20
        );
        assertThrows(Exception.class, () -> orderFacade.orderAndPay(userId, request));
        // 재고, 포인트 모두 변하지 않아야 함
        assertThat(productMetaRepository.findByProductId(productId).get().getStock()).isEqualTo(10L);
        assertThat(userPointRepository.findByUserId(userId).get().getBalance()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("포인트 부족 시 전체 롤백")
    void shouldRollback_whenPointNotEnough() {
        // 포인트 10_000, 상품 2개 주문(2만)
        OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                Collections.singletonList(new OrderV1Dto.OrderItem(productId, 2L))
        );
        assertThrows(Exception.class, () -> orderFacade.orderAndPay(userId, request));
        assertThat(productMetaRepository.findByProductId(productId).get().getStock()).isEqualTo(10L);
        assertThat(userPointRepository.findByUserId(userId).get().getBalance()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("정상 주문 시 모든 처리 정상 반영")
    void shouldCommit_whenOrderSuccess() {
        OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L))
        );
        orderFacade.orderAndPay(userId, request);
        assertThat(productMetaRepository.findByProductId(productId).get().getStock()).isEqualTo(9L);
        assertThat(userPointRepository.findByUserId(userId).get().getBalance()).isEqualTo(0L);
    }
}
