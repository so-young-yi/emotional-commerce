package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "User Point V1 API", description = "유저 포인트 API 입니다.")
public interface UserPointV1ApiSpec {

    @Operation(
            summary = "유저 포인트 조회",
            description = "유저 ID로 보유 포인트를 조회합니다."
    )
    ApiResponse<UserPointV1Dto.PointResponse> getUserPoint(
            @RequestHeader("X-USER-ID") Long userId
    );

    @Operation(
            summary = "유저 포인트 충전",
            description = "유저 ID와 충전 금액을 받아 포인트를 충전합니다."
    )
    ApiResponse<UserPointV1Dto.PointResponse> chargeUserPoint(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody UserPointV1Dto.PointChargeRequest request
    );
}
