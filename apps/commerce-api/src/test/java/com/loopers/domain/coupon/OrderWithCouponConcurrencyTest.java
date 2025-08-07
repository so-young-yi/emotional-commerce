package com.loopers.domain.coupon;

import com.loopers.application.coupon.CouponFacade;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("OrderFacade + Coupon 통합/동시성 테스트")
class OrderWithCouponConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired private CouponFacade couponFacade;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductMetaRepository productMetaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;

    private Long userId;
    private Long productId;
    private Long couponId;

    @BeforeEach
    void setUp() {
        UserModel user = userRepository.save(new UserModel("user", "유저", "user@email.com", "2000-01-01", Gender.F));
        userId = user.getId();
        userPointRepository.save(new UserPointModel(userId, 10_000L));
        ProductModel product = productRepository.save(new ProductModel(
                null, 1L, "상품", "설명", new Money(10_000L), ProductStatus.ON_SALE, ZonedDateTime.now()
        ));
        productId = product.getId();
        productMetaRepository.save(ProductMetaModel.builder().productId(productId).stock(10L).likeCount(0L).reviewCount(0L).viewCount(0L).build());
        CouponModel coupon = couponRepository.save(new CouponModel(CouponType.AMOUNT, 2000L, null, "2천원 할인쿠폰"));
        couponId = coupon.getId();
        couponFacade.issueCoupon(userId, couponId);
    }

    @Test
    @DisplayName("정상 주문+쿠폰 적용")
    void orderWithCoupon_success() {
        OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L,null)),
                couponId
        );
        orderFacade.orderAndPay(userId, request);

        assertThat(productMetaRepository.findByProductId(productId).get().getStock()).isEqualTo(9L);
        assertThat(userPointRepository.findByUserId(userId).get().getBalance()).isEqualTo(2_000L);
        assertThat(userCouponRepository.findByUserIdAndCouponId(userId, couponId).get().isUsed()).isTrue();
    }

    @Test
    @DisplayName("동시에 여러 기기에서 쿠폰을 사용해도 단 1회만 사용된다 (낙관적 락)")
    void shouldOnlyUseCouponOnce_whenConcurrentOrderWithCoupon() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> successList = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                            Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L,null)),
                            couponId
                    );
                    try {
                        orderFacade.orderAndPay(userId, request);
                        successList.add(true);
                    } catch (Exception e) {
                        successList.add(false);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        long successCount = successList.stream().filter(b -> b).count();
        long failCount = successList.stream().filter(b -> !b).count();

        assertThat(successCount).isEqualTo(1L);
        assertThat(failCount).isEqualTo(9L);
        assertThat(userCouponRepository.findByUserIdAndCouponId(userId, couponId).get().isUsed()).isTrue();
    }
}
