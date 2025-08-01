package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<UserModel> findById(Long id);
    Optional<UserModel> findByLoginId(String loginId);
    UserModel save(UserModel user);
}
