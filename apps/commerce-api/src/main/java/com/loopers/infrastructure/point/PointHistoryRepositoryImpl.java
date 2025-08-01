package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryModel;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public Optional<PointHistoryModel> findById(Long id) {
        return pointHistoryJpaRepository.findById(id);
    }

    @Override
    public PointHistoryModel save(PointHistoryModel history) {
        return pointHistoryJpaRepository.save(history);
    }

    @Override
    public Page<PointHistoryModel> findByUserId(Long userId, Pageable pageable) {
        return pointHistoryJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<PointHistoryModel> findAll(Pageable pageable) {
        return pointHistoryJpaRepository.findAll(pageable);
    }
}
