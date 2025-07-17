package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<UserModel> findById(Long id);
    Optional<UserModel> findByUserId(String userId);
    UserModel save(UserModel user);
}
