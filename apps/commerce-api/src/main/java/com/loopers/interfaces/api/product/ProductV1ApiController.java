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

        List<ProductV1Dto.ProductSummaryResponse> items = productFacade.getProductList(ProductSearchCriteria.from(request));
        ProductV1Dto.ProductListPageResponse response = new ProductV1Dto.ProductListPageResponse(
                items, items.size(), 1, request.page() != null ? request.page() : 0, request.size() != null ? request.size() : 20
        );
        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductDetailResponse> getProduct(@PathVariable Long productId) {

        ProductV1Dto.ProductDetailResponse response = productFacade.getProductDetail(productId);
        return ApiResponse.success(response);
    }

}
