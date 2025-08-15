package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@Table(name = "point_history")
@Entity
@Getter
public class PointHistoryModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long amount;
    private String reason;
    private ZonedDateTime createdAt;

    protected PointHistoryModel() {}

    public PointHistoryModel(Long userId, Long amount, String reason) {
        if (userId == null || userId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST,"유저 포인트 ID는 필수입니다.");
        if (amount == null)
            throw new CoreException(ErrorType.BAD_REQUEST,"포인트 변동 금액은 필수입니다.");
        if (reason == null || reason.isBlank())
            throw new CoreException(ErrorType.BAD_REQUEST,"사유는 필수입니다.");
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = ZonedDateTime.now();
    }
}
