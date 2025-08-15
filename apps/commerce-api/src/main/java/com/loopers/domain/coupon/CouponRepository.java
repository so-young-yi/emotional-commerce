package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {

    Optional<CouponModel> findById(Long couponId);
    CouponModel save(CouponModel couponModel);
}
