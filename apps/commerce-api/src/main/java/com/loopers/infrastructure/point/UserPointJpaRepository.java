package com.loopers.infrastructure.point;

import com.loopers.domain.point.UserPointModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointJpaRepository extends JpaRepository<UserPointModel,Long> {

    Optional<UserPointModel> findByUserId(Long userId);
}
