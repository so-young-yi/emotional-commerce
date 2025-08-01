package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_point")
@Getter
@NoArgsConstructor
public class UserPointModel{

    @Id
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    public UserPointModel(Long userId, Long point) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID는 필수입니다.");
        }
        if (point == null || point < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 0 이상이어야 합니다.");
        }
        this.userId = userId;
        this.balance = point;
    }

    public void charge(Long amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 1 이상이어야 합니다.");
        }
        this.balance += amount;
    }

    public void use(Long amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용 금액은 1 이상이어야 합니다.");
        }
        if (this.balance < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        this.balance -= amount;
    }
}
