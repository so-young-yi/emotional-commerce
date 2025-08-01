package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Product V1 Api", description = "상품 api")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품목록", description = "상품 목록 조회입니다.(페이징 포함)")
    ApiResponse<ProductV1Dto.ProductListPageResponse> getProducts( ProductV1Dto.ProductRequest request );

    @Operation(summary = "상품상세", description = "상품 단건 상세입니다.")
    ApiResponse<ProductV1Dto.ProductDetailResponse> getProduct( Long productId );

}
