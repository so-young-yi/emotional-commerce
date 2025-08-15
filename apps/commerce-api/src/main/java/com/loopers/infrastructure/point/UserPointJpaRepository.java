package com.loopers.infrastructure.point;

import com.loopers.domain.point.UserPointModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointJpaRepository extends JpaRepository<UserPointModel,Long> {

    Optional<UserPointModel> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserPointModel u where u.userId = :userId")
    Optional<UserPointModel> findByUserIdForUpdate(@Param("userId") Long userId);
}
