package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private Long amount;      // 정액 쿠폰일 때만 사용
    private Integer percent;  // 정률 쿠폰일 때만 사용 (예: 10 = 10%)

    private String name;

    public CouponModel(CouponType type, Long amount, Integer percent, String name) {
        if (type == null) throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입은 필수입니다.");
        if (type == CouponType.AMOUNT && (amount == null || amount <= 0))
            throw new CoreException(ErrorType.BAD_REQUEST, "정액 쿠폰은 금액이 0보다 커야 합니다.");
        if (type == CouponType.PERCENT && (percent == null || percent <= 0 || percent > 100))
            throw new CoreException(ErrorType.BAD_REQUEST, "정률 쿠폰은 1~100%여야 합니다.");
        if (name == null || name.isBlank())
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰명은 필수입니다.");
        this.type = type;
        this.amount = amount;
        this.percent = percent;
        this.name = name;
    }

    public long calculateDiscount(long orderAmount) {
        if (type == CouponType.AMOUNT) {
            return Math.min(amount, orderAmount);
        } else if (type == CouponType.PERCENT) {
            return orderAmount * percent / 100;
        }
        return 0L;
    }
}
