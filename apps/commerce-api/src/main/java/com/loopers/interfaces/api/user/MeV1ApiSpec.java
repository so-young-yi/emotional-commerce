package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Me V1 API", description = "회원 정보를 조회합니다." )
public interface MeV1ApiSpec {

    @Operation(summary = "회원조회", description = "회원 정보를 조회합니다.")
    ApiResponse<UserV1Dto.UserResponse> me(Long id);

}
