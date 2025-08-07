package com.loopers.interfaces.api.point;


import com.loopers.application.point.UserPointFacade;
import com.loopers.application.point.UserPointInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class UserPointV1ApiController implements UserPointV1ApiSpec {

    private final UserPointFacade userPointFacade;

    @GetMapping
    @Override
    public ApiResponse<UserPointV1Dto.PointResponse> getUserPoint(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        UserPointInfo userPointInfo = userPointFacade.getUserPoint(userId);
        UserPointV1Dto.PointResponse response = UserPointV1Dto.PointResponse.from(userPointInfo);
        return ApiResponse.success(response);
    }

    @PostMapping("/charge")
    @Override
    public ApiResponse<UserPointV1Dto.PointResponse> chargeUserPoint(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody UserPointV1Dto.PointChargeRequest request
    ) {
        UserPointInfo userPointInfo = userPointFacade.chargeUserPoint(userId, request.amount());
        UserPointV1Dto.PointResponse response = UserPointV1Dto.PointResponse.from(userPointInfo);
        return ApiResponse.success(response);
    }
}
