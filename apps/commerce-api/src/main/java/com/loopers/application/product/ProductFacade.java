package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.*;
import com.loopers.interfaces.api.product.ProductV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final ProductMetaService productMetaService;
    private final BrandService brandService;

    public ProductV1Dto.ProductListPageResponse getProductList(ProductSearchCriteria criteria) {
        Page<ProductSummaryInfo> productPage = productService.getProductSummaries(criteria);
        List<ProductSummaryInfo> products = productPage.getContent();
        List<Long> brandIds = products.stream().map(ProductSummaryInfo::brandId).distinct().toList();
        Map<Long, String> brandNameMap = brandIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            var brand = brandService.getBrand(id);
                            return brand != null ? brand.getName() : null;
                        }
                ));

        List<ProductV1Dto.ProductSummaryResponse> dtos = products.stream()
                .map(p -> new ProductV1Dto.ProductSummaryResponse(
                        p.id(),
                        p.name(),
                        brandNameMap.get(p.brandId()),
                        p.sellPrice(),
                        p.stock(),
                        p.status(),
                        p.likeCount()
                ))
                .collect(Collectors.toList());

        return new ProductV1Dto.ProductListPageResponse(
                dtos,
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
        );
    }

    public ProductV1Dto.ProductDetailResponse getProductDetail(Long productId) {
        ProductDetailProjection detail = productService.getProductDetail(productId);

        String brandName = null;
        if (detail.getBrandId() != null) {
            var brand = brandService.getBrand(detail.getBrandId());
            brandName = brand != null ? brand.getName() : null;
        }

        return new ProductV1Dto.ProductDetailResponse(
                detail.getId(),
                detail.getName(),
                brandName,
                detail.getSellPrice(),
                detail.getStock(),
                detail.getStatus(),
                detail.getLikeCount() != null ? detail.getLikeCount() : 0L
        );
    }
}
