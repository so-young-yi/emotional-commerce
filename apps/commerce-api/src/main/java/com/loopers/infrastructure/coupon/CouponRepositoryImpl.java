package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<CouponModel> findById(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public CouponModel save(CouponModel couponModel) {
        return couponJpaRepository.save(couponModel);
    }
}
