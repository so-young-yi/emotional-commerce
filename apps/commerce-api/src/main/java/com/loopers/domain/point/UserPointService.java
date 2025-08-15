package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserPointService {

    private final UserPointRepository userPointRepository;

    @Transactional(readOnly = true)
    public UserPointModel getUserPoint(Long userId) {
       return userPointRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public UserPointModel chargeUserPoint(Long userId, int point) {
        UserPointModel userPoint = getUserPoint(userId);
        userPoint.charge(point);
        return userPoint;
    }

}
