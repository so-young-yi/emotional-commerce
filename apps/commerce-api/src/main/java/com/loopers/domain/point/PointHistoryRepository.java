package com.loopers.domain.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PointHistoryRepository {

    Optional<PointHistoryModel> findById(Long id);

    PointHistoryModel save(PointHistoryModel history);

    Page<PointHistoryModel> findByUserId(Long userPointId, Pageable pageable);

    Page<PointHistoryModel> findAll(Pageable pageable);
}
