package com.loopers.interfaces.api.point;

import com.loopers.application.point.UserPointInfo;

public class UserPointV1Dto {

    public record PointResponse(Integer point
    ) {
        public static PointResponse from(UserPointInfo info) {
            return new PointResponse(
                    info.point()
            );
        }
    }

    public record PointChargeRequest(Integer amount
    ) {

    }
}
