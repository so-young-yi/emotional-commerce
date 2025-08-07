package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("쿠폰 도메인 단위 테스트")
class CouponDomainUnitTest {

    @Nested
    @DisplayName("CouponModel (쿠폰 마스터)")
    class CouponModelTest {

        @Test
        @DisplayName("정액 쿠폰 생성 시 금액이 0 이하이면 예외")
        void shouldFail_whenAmountCouponAmountInvalid() {
            assertThrows(CoreException.class, () -> new CouponModel(CouponType.AMOUNT, 0L, null, "정액쿠폰"));
        }

        @Test
        @DisplayName("정률 쿠폰 생성 시 percent가 1~100이 아니면 예외")
        void shouldFail_whenPercentCouponPercentInvalid() {
            assertThrows(CoreException.class, () -> new CouponModel(CouponType.PERCENT, null, 0, "정률쿠폰"));
            assertThrows(CoreException.class, () -> new CouponModel(CouponType.PERCENT, null, 101, "정률쿠폰"));
        }

        @Test
        @DisplayName("정액 쿠폰 할인 계산")
        void amountCouponDiscount() {
            CouponModel coupon = new CouponModel(CouponType.AMOUNT, 2000L, null, "2천원쿠폰");
            assertThat(coupon.calculateDiscount(10_000L)).isEqualTo(2000L);
            assertThat(coupon.calculateDiscount(1_000L)).isEqualTo(1000L); // 주문금액보다 쿠폰이 크면 주문금액만 할인
        }

        @Test
        @DisplayName("정률 쿠폰 할인 계산")
        void percentCouponDiscount() {
            CouponModel coupon = new CouponModel(CouponType.PERCENT, null, 10, "10%쿠폰");
            assertThat(coupon.calculateDiscount(10_000L)).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("UserCouponModel (유저 쿠폰)")
    class UserCouponModelTest {

        @Test
        @DisplayName("쿠폰 발급 시 유저/쿠폰 ID가 0 이하이면 예외")
        void shouldFail_whenUserOrCouponIdInvalid() {
            assertThrows(CoreException.class, () -> new UserCouponModel(0L, 1L));
            assertThrows(CoreException.class, () -> new UserCouponModel(1L, 0L));
        }

        @Test
        @DisplayName("쿠폰은 한 번만 사용할 수 있다")
        void shouldFail_whenUseTwice() {
            UserCouponModel userCoupon = new UserCouponModel(1L, 1L);
            userCoupon.use();
            assertThrows(CoreException.class, userCoupon::use);
        }
    }
}
