package com.loopers.domain.point;

import java.util.Optional;

public interface UserPointRepository {

    Optional<UserPointModel> findByUserId(Long userId);
    Optional<UserPointModel> findByUserIdForUpdate(Long userId);
    UserPointModel save(UserPointModel userPointModel);
}
