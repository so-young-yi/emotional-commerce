package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserPointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public UserPointModel getUserPoint(Long userId) {
       return userPointRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public UserPointModel chargeUserPoint(Long userId, Long point) {
        UserPointModel userPoint = getUserPoint(userId);
        userPoint.charge(point);
        return userPoint;
    }

    public void useUserPoint(Long userId, Long amount) {
        UserPointModel userPoint = userPointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보 없음"));
        if (userPoint.getBalance() < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 부족");
        }
        userPoint.use(amount);
        userPointRepository.save(userPoint);

        pointHistoryRepository.save(new PointHistoryModel(userPoint.getUserId(), -amount, "주문 결제 차감"));
    }

    public void refundUserPoint(Long userId, Long amount) {
        UserPointModel userPoint = userPointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 존재하지 않습니다."));
        userPoint.charge(amount); // 포인트 환불(충전)
        userPointRepository.save(userPoint);

        pointHistoryRepository.save(
                new PointHistoryModel(userId, amount, "주문 결제 환불")
        );
    }

}
