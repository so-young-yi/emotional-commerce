package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Product Like V1 Api", description = "상품 좋아요 api")
public interface ProductLikeV1ApiSpec {

    @Operation(summary = "상품 좋아요 등록", description = "상품에 좋아요를 등록합니다.")
    ApiResponse<Boolean> likeProduct(@RequestHeader("X-USER-ID") Long userId,
                                    @PathVariable Long productId);

    @Operation(summary = "상품 좋아요 해제", description = "상품에 좋아요를 해제합니다.")
    ApiResponse<Boolean> unlikeProduct(@RequestHeader("X-USER-ID") Long userId,
                                     @PathVariable Long productId);
}
