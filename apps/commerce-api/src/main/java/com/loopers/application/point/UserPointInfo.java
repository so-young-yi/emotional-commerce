package com.loopers.application.point;

import com.loopers.domain.point.UserPointModel;

public record UserPointInfo(Long userId, Integer point) {

    public static UserPointInfo from(UserPointModel model) {
        return new UserPointInfo(
                model.getUserId(),
                model.getPoint()
        );
    }
}
