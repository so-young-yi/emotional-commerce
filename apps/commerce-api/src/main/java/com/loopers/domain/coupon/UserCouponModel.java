package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_coupon", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "couponId"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long couponId;
    private boolean used = false;

    @Version
    private Long version; // 낙관적 락

    public UserCouponModel(Long userId, Long couponId) {
        if (userId == null || userId <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID는 필수입니다.");
        if (couponId == null || couponId <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 ID는 필수입니다.");
        this.userId = userId;
        this.couponId = couponId;
        this.used = false;
    }

    public void use() {
        if (used) throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        this.used = true;
    }

    public void restore() {
        this.used = false;
    }

}
