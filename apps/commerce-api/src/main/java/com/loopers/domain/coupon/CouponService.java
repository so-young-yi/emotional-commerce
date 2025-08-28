package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));
        userCouponRepository.findByUserIdAndCoupon(userId, coupon)
                .ifPresent(uc -> { throw new CoreException(ErrorType.BAD_REQUEST, "이미 발급된 쿠폰입니다."); });
        userCouponRepository.save(new UserCouponModel(userId, coupon));
    }

    @Transactional
    public long useCoupon(Long userId, Long couponId, long orderAmount) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCoupon(userId, coupon)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
        if (userCoupon.isUsed()) throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");

        long discount = userCoupon.calculateDiscount(orderAmount);
        userCoupon.use();
        userCouponRepository.save(userCoupon);
        return discount;
    }

    @Transactional(readOnly = true)
    public long calculateDiscount(Long userId, Long couponId, long orderAmount) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCoupon(userId, coupon)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
        return userCoupon.calculateDiscount(orderAmount);
    }

    @Transactional(readOnly = true)
    public List<UserCouponModel> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserId(userId);
    }

    @Transactional
    public void restoreCoupon(Long userId, Long couponId) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCoupon(userId, coupon)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
        if (!userCoupon.isUsed()) return;
        userCoupon.restore();
        userCouponRepository.save(userCoupon);
    }

    @Transactional(readOnly = true)
    public boolean isCouponUsable(Long userId, Long couponId) {
        return couponRepository.findById(couponId)
                .flatMap(coupon -> userCouponRepository.findByUserIdAndCoupon(userId, coupon))
                .map(uc -> !uc.isUsed())
                .orElse(false);
    }

    // TODO: 만료 정책 구현 (예: 만료일 필드 추가 후 today 기준 만료 처리)
    public void expireCoupons(LocalDate today) { }
}
