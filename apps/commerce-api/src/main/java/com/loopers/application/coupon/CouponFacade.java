package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.UserCouponModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponService couponService;

    public void issueCoupon(Long userId, Long couponId) {
        couponService.issueCoupon(userId, couponId);
    }

    public long useCoupon(Long userId, Long couponId, long orderAmount) {
        return couponService.useCoupon(userId, couponId, orderAmount);
    }

    public List<UserCouponModel> getUserCoupons(Long userId) {
        return couponService.getUserCoupons(userId);
    }

    public long calculateDiscount(Long couponId, long orderAmount) {
        return couponService.calculateDiscount(couponId, orderAmount);
    }

    public void restoreCoupon(Long userId, Long couponId) {
        couponService.restoreCoupon(userId, couponId);
    }
}
