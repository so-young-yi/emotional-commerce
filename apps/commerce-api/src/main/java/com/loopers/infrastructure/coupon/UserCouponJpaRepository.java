package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCouponModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponModel,Long> {

    Optional<UserCouponModel> findByUserIdAndCouponId(Long userId, Long couponId);
    UserCouponModel save(UserCouponModel userCouponModel);
    List<UserCouponModel> findAllByUserId(Long userId);

}
