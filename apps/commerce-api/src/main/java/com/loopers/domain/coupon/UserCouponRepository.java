package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCouponModel> findByUserIdAndCouponId(Long userId, Long couponId);
    UserCouponModel save(UserCouponModel userCouponModel);
    List<UserCouponModel> findAllByUserId(Long userId);
    // TODO 만료일
}
