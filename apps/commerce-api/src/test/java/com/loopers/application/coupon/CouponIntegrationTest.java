package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("CouponFacade 통합 테스트")
class CouponIntegrationTest {

    @Autowired private CouponFacade couponFacade;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @AfterEach void tearDown() { databaseCleanUp.truncateAllTables(); }

    private Long userId = 1L;
    private Long couponId;

    @BeforeEach
    void setUp() {
        CouponModel coupon = couponRepository.save(new CouponModel(CouponType.AMOUNT, 2000L, null, "2천원 할인쿠폰"));
        couponId = coupon.getId();
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_success() {
        couponFacade.issueCoupon(userId, couponId);
        assertThat(userCouponRepository.findByUserIdAndCouponId(userId, couponId)).isPresent();
    }

    @Test
    @DisplayName("중복 발급 시 예외")
    void issueCoupon_duplicate() {
        couponFacade.issueCoupon(userId, couponId);
        assertThrows(CoreException.class, () -> couponFacade.issueCoupon(userId, couponId));
    }

    @Test
    @DisplayName("쿠폰 사용 성공 및 할인액 반환")
    void useCoupon_success() {
        couponFacade.issueCoupon(userId, couponId);
        long discount = couponFacade.useCoupon(userId, couponId, 10_000L);
        assertThat(discount).isEqualTo(2000L);
        assertThat(userCouponRepository.findByUserIdAndCouponId(userId, couponId).get().isUsed()).isTrue();
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 예외")
    void useCoupon_alreadyUsed() {
        couponFacade.issueCoupon(userId, couponId);
        couponFacade.useCoupon(userId, couponId, 10_000L);
        assertThrows(CoreException.class, () -> couponFacade.useCoupon(userId, couponId, 10_000L));
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰은 예외")
    void useCoupon_notExist() {
        assertThrows(CoreException.class, () -> couponFacade.useCoupon(userId, 9999L, 10_000L));
    }
}
