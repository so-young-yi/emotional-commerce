package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;

    public UserInfo signup(UserV1Dto.SignUpRequest request) {
        return UserInfo.from(userService.signup(request.toModel()));
    }

    public UserInfo getUserInfoById(Long id) {
        return UserInfo.from(userService.getUserById(id));
    }
}
