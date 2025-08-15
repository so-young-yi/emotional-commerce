package com.loopers.application.order;


// 검색 조건 DTO
public record OrderSearchCriteria(
        Long userId,
        String sort,
        Integer page,
        Integer size
){}

