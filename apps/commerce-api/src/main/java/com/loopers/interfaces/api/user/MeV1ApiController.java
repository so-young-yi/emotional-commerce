package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me")
public class MeV1ApiController implements MeV1ApiSpec {

    private final UserFacade userFacade;

    @GetMapping("/{id}")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> me(@PathVariable Long id) {

        UserInfo userInfo = userFacade.getUserInfoById(id);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userInfo);
        return ApiResponse.success(response);
    }
}
