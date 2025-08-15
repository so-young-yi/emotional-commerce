package com.loopers.infrastructure.point;

import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserPointRepositoryImpl implements UserPointRepository {

    private final UserPointJpaRepository userPointJpaRepository;

    @Override
    public Optional<UserPointModel> findByUserId(Long userId) {
        return userPointJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserPointModel> findByUserIdForUpdate(Long userId) {
        return userPointJpaRepository.findByUserIdForUpdate(userId);
    }

    @Override
    public UserPointModel save(UserPointModel userPointModel) {
        return userPointJpaRepository.save(userPointModel);
    }
}
