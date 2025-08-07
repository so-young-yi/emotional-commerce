package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products/{productId}/likes")
public class ProductLikeV1ApiController implements ProductLikeV1ApiSpec {

    private final ProductLikeFacade productLikeFacade;

    @PostMapping
    @Override
    public ApiResponse<ProductLikeV1Dto.ProductLikeResponse> likeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    ) {
        boolean liked = productLikeFacade.likeProduct(userId, productId);
        return ApiResponse.success(new ProductLikeV1Dto.ProductLikeResponse(userId, productId, liked));
    }

    @DeleteMapping
    @Override
    public ApiResponse<ProductLikeV1Dto.ProductLikeResponse> unlikeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    ) {
        boolean liked = productLikeFacade.unlikeProduct(userId, productId);
        return ApiResponse.success(new ProductLikeV1Dto.ProductLikeResponse(userId, productId, liked));
    }
}
