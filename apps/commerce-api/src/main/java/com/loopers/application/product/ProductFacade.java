package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.interfaces.api.product.ProductV1Dto;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final ProductLikeService productLikeService;
    private final BrandService brandService;

    public ProductV1Dto.ProductListPageResponse getProductList(ProductSearchCriteria criteria) {
        Page<ProductModel> productPage = productService.getProducts(criteria);
        List<ProductModel> products = productPage.getContent();

        List<Long> productIds = products.stream().map(ProductModel::getId).toList();
        Map<Long, Long> likeCountMap = productLikeService.getLikeCountsForProductIds(productIds);

        List<Long> brandIds = products.stream().map(ProductModel::getBrandId).distinct().toList();
        Map<Long, String> brandNameMap = brandIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            BrandModel brand = brandService.getBrand(id);
                            return brand != null ? brand.getName() : null;
                        }
                ));

        List<ProductV1Dto.ProductSummaryResponse> dtos = products.stream()
                .map(product -> new ProductV1Dto.ProductSummaryResponse(
                        product.getId(),
                        product.getName(),
                        brandNameMap.get(product.getBrandId()),
                        product.getSellPrice().getAmount(),
                        product.getStock(),
                        product.getStatus().name(),
                        likeCountMap.getOrDefault(product.getId(), 0L)
                ))
                .collect(Collectors.toList());

        if ("likes_desc".equals(criteria.sort())) {
            dtos = dtos.stream()
                    .sorted(Comparator.comparing(ProductV1Dto.ProductSummaryResponse::likeCount).reversed())
                    .collect(Collectors.toList());
        }

        return new ProductV1Dto.ProductListPageResponse(
                dtos,
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
        );
    }

    public ProductV1Dto.ProductDetailResponse getProductDetail(Long productId) {
        ProductModel product = productService.getProductDetail(productId);
        long likeCount = productLikeService.getLikeCountOfProduct(productId);
        String brandName = null;
        if (product.getBrandId() != null) {
            BrandModel brand = brandService.getBrand(product.getBrandId());
            brandName = brand != null ? brand.getName() : null;
        }

        return new ProductV1Dto.ProductDetailResponse(
                product.getId(),
                product.getName(),
                brandName,
                product.getSellPrice().getAmount(),
                product.getStock(),
                product.getStatus().name(),
                likeCount
        );
    }
}
