package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        if (userCouponRepository.findByUserIdAndCouponId(userId, couponId).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 발급된 쿠폰입니다.");
        }
        userCouponRepository.save(new UserCouponModel(userId, couponId));
    }

    @Transactional
    public long useCoupon(Long userId, Long couponId, long orderAmount) {
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
        if (userCoupon.isUsed()) throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");

        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));

        long discount = coupon.calculateDiscount(orderAmount);
        userCoupon.use();
        userCouponRepository.save(userCoupon);
        return discount;
    }

    @Transactional(readOnly = true)
    public long calculateDiscount(Long couponId, long orderAmount) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));
        return coupon.calculateDiscount(orderAmount);
    }

    @Transactional(readOnly = true)
    public List<UserCouponModel> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserId(userId);
    }

    @Transactional
    public void restoreCoupon(Long userId, Long couponId) {
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
        if (!userCoupon.isUsed()) return; // 이미 미사용이면 무시
        userCoupon.restore(); // 복구
        userCouponRepository.save(userCoupon);
    }

    public boolean isCouponUsable(Long userId, Long couponId) {
        Optional<UserCouponModel> userCouponOpt = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
        return userCouponOpt.isPresent() && !userCouponOpt.get().isUsed();
    }

    // TODO 쿠폰 만료 처리 (예: 만료일이 지난 쿠폰을 일괄 만료)
    public void expireCoupons(LocalDate today) {
    }

}
