package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCouponModel> findByUserIdAndCoupon(Long userId, CouponModel coupon);
    UserCouponModel save(UserCouponModel userCouponModel);
    List<UserCouponModel> findAllByUserId(Long userId);
}
