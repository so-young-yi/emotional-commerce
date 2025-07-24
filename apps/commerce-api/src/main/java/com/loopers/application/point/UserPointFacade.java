package com.loopers.application.point;

import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserPointFacade {

    private final UserService userService;
    private final UserPointService userPointService;

    public UserPointInfo getUserPoint(Long userId) {
        if ( userService.getUserById(userId) == null ) {
            return null;
        }
        return UserPointInfo.from(userPointService.getUserPoint(userId));
    }

    public UserPointInfo chargeUserPoint(Long userId, Integer amount) {
        if ( userService.getUserById(userId) == null ) {
            throw new CoreException(ErrorType.NOT_FOUND, "[userId = " + userId + "] 사용자를 찾을 수 없습니다.");
        }
        return UserPointInfo.from(userPointService.chargeUserPoint(userId, amount));
    }
}
