package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryModel, Long> {

    Page<PointHistoryModel> findByUserId(Long userId, Pageable pageable);

    Page<PointHistoryModel> findAll(Pageable pageable);
}
