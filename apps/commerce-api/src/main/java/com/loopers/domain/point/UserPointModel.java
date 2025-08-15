package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_point")
@Getter
@NoArgsConstructor
public class UserPointModel {

    @Id
    private Long userId;
    private Integer point;

    public UserPointModel(Long userId, Integer point) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID는 필수입니다.");
        }
        if (point == null || point < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 0 이상이어야 합니다.");
        }
        this.userId = userId;
        this.point = point;
    }

    public void charge(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 1 이상이어야 합니다.");
        }
        this.point += amount;
    }

    public void use(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용 금액은 1 이상이어야 합니다.");
        }
        if (this.point < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        this.point -= amount;
    }
}
