package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductSearchCriteria;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1ApiController implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @GetMapping
    @Override
    public ApiResponse<ProductV1Dto.ProductListPageResponse> getProducts(@ModelAttribute ProductV1Dto.ProductRequest request) {
        ProductV1Dto.ProductListPageResponse response = productFacade.getProductList(ProductSearchCriteria.from(request));
        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductDetailResponse> getProduct(@PathVariable Long productId) {
        ProductV1Dto.ProductDetailResponse response = productFacade.getProductDetail(productId);
        return ApiResponse.success(response);
    }
}
