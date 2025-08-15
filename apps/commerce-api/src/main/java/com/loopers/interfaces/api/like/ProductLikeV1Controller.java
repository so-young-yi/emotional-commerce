package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products/{productId}/likes")
public class ProductLikeV1Controller implements ProductLikeV1ApiSpec {

    private final ProductLikeFacade productLikeFacade;

    @PostMapping
    @Override
    public ApiResponse<Boolean> likeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    ) {
        boolean liked = productLikeFacade.likeProduct(userId, productId);
        return ApiResponse.success(liked);
    }

    @DeleteMapping
    @Override
    public ApiResponse<Boolean> unlikeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    ) {
        boolean liked = productLikeFacade.unlikeProduct(userId, productId);
        return ApiResponse.success(liked);
    }

}
