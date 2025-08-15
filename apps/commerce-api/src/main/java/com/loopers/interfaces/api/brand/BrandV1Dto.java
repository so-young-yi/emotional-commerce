package com.loopers.interfaces.api.brand;

import java.util.List;

public class BrandV1Dto {

    public record BrandRequest(
            String name,      // 브랜드명 검색(선택)
            Integer page,     // 페이징(선택)
            Integer size
    ) {}

    public record BrandSummaryResponse(
            Long id,
            String name,
            String description
    ) {
        public static BrandSummaryResponse of(com.loopers.domain.brand.BrandModel brand) {
            return new BrandSummaryResponse(
                    brand.getId(),
                    brand.getName(),
                    brand.getDescription()
            );
        }
    }

    public record BrandListPageResponse(
            List<BrandSummaryResponse> items,
            long totalCount,
            int totalPages,
            int page,
            int size
    ) {}

    public record BrandDetailResponse(
            Long id,
            String name,
            String description
    ) {
        public static BrandDetailResponse of(com.loopers.domain.brand.BrandModel brand) {
            return new BrandDetailResponse(
                    brand.getId(),
                    brand.getName(),
                    brand.getDescription()
            );
        }
    }
}
