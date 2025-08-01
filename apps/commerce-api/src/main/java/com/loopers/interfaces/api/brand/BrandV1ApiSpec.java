package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Brand V1 Api", description = "브랜드 api")
public interface BrandV1ApiSpec {

    @Operation(summary = "브랜드", description = "브랜드 목록 조회입니다.(페이징 포함)")
    ApiResponse<BrandV1Dto.BrandListPageResponse> getBrands( BrandV1Dto.BrandRequest request );

    @Operation(summary = "브랜드", description = "브랜드 단건 상세입니다.")
    ApiResponse<BrandV1Dto.BrandDetailResponse> getBrand( Long brandId );



}
