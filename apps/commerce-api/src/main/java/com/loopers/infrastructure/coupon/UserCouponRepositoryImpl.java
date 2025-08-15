package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public Optional<UserCouponModel> findByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId,couponId);
    }

    @Override
    public UserCouponModel save(UserCouponModel userCouponModel) {
        return userCouponJpaRepository.save(userCouponModel);
    }

    @Override
    public List<UserCouponModel> findAllByUserId(Long userId) {
        return userCouponJpaRepository.findAllByUserId(userId);
    }

}
