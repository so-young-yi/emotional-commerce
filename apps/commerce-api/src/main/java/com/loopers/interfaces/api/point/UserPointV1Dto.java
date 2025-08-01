package com.loopers.interfaces.api.point;

import com.loopers.application.point.UserPointInfo;

public class UserPointV1Dto {

    public record PointResponse(Long point
    ) {
        public static PointResponse from(UserPointInfo info) {
            return new PointResponse(
                    info.point()
            );
        }
    }

    public record PointChargeRequest(Long amount
    ) {

    }
}
