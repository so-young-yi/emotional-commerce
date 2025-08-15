package com.loopers.application.product;

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

    // 상품 목록 조회 (좋아요 수 포함, 정렬)
    public List<ProductV1Dto.ProductSummaryResponse> getProductList(ProductSearchCriteria criteria) {
        Page<ProductModel> productPage = productService.getProducts(criteria);
        List<ProductModel> products = productPage.getContent();

        // 좋아요 수를 한 번에 조회 (상품 ID 리스트로)
        List<Long> productIds = products.stream().map(ProductModel::getId).toList();
        Map<Long, Long> likeCountMap = productLikeService.getLikeCountsForProductIds(productIds);

        // DTO 변환
        List<ProductV1Dto.ProductSummaryResponse> dtos = products.stream()
                .map(product -> new ProductV1Dto.ProductSummaryResponse(
                        product.getId(),
                        product.getName(),
                        null, // brandName 등
                        product.getSellPrice().getAmount(),
                        product.getStock(),
                        product.getStatus().name(),
                        likeCountMap.getOrDefault(product.getId(), 0L)
                ))
                .collect(Collectors.toList());

        // 정렬
        if ("likes_desc".equals(criteria.sort())) {
            dtos = dtos.stream()
                    .sorted(Comparator.comparing(ProductV1Dto.ProductSummaryResponse::likeCount).reversed())
                    .collect(Collectors.toList());
        }
        // 최신순, 가격순 등은 이미 DB에서 정렬됨

        return dtos;
    }

    // 상품 상세 조회 (좋아요 수 포함)
    public ProductV1Dto.ProductDetailResponse getProductDetail(Long productId) {
        ProductModel product = productService.getProductDetail(productId);
        long likeCount = productLikeService.getLikeCountOfProduct(productId);

        return new ProductV1Dto.ProductDetailResponse(
                product.getId(),
                product.getName(),
                null, // brandName은 필요시 추가
                product.getSellPrice().getAmount(),
                product.getStock(),
                product.getStatus().name(),
                likeCount
        );
    }
}
