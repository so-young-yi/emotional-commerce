package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductMetaModel;
import com.loopers.domain.product.ProductMetaService;
import com.loopers.domain.product.ProductService;
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
    private final ProductMetaService productMetaService;
    private final BrandService brandService;

    public ProductV1Dto.ProductListPageResponse getProductList(ProductSearchCriteria criteria) {
        Page<ProductModel> productPage = productService.getProducts(criteria);
        List<ProductModel> products = productPage.getContent();

        List<Long> productIds = products.stream().map(ProductModel::getId).toList();
        Map<Long, ProductMetaModel> metaMap = productMetaService.getMetasForProductIds(productIds);

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
                .map(product -> {
                    ProductMetaModel meta = metaMap.get(product.getId());
                    return new ProductV1Dto.ProductSummaryResponse(
                            product.getId(),
                            product.getName(),
                            brandNameMap.get(product.getBrandId()),
                            product.getSellPrice().getAmount(),
                            meta != null ? meta.getStock() : 0L,
                            product.getStatus().name(),
                            meta != null ? meta.getLikeCount() : 0L
                    );
                })
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
        ProductMetaModel meta = productMetaService.getMeta(productId);

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
                meta != null ? meta.getStock() : 0L,
                product.getStatus().name(),
                meta != null ? meta.getLikeCount() : 0L
        );
    }
}
