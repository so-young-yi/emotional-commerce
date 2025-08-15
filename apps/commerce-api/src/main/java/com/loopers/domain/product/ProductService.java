package com.loopers.domain.product;

import com.loopers.application.product.ProductSearchCriteria;
import com.loopers.application.product.ProductSummaryInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(
            cacheNames = "product:list",
            key = "#criteria.brandId + '-' + #criteria.sortType + '-' + #criteria.getPageOrDefault() + '-' + #criteria.getSizeOrDefault()"
    )
    public Page<ProductSummaryInfo> getProductSummaries(ProductSearchCriteria criteria) {

        int page = criteria.getPageOrDefault();
        int size = criteria.getSizeOrDefault();
        ProductSortType sortType = criteria.getSortTypeOrDefault();
        Sort sortObj = Sort.by(sortType.getDirection(), sortType.getSortField());
        PageRequest pageRequest = PageRequest.of(page, size, sortObj);

        Page<ProductSummaryProjection> projections = productRepository.findProductSummaries(criteria.brandId(), pageRequest);

        List<ProductSummaryInfo> dtoList = projections.getContent().stream()
                .map(ProductSummaryInfo::from)
                .toList();

        return new PageImpl<>(dtoList, pageRequest, projections.getTotalElements());
    }

    public ProductDetailProjection getProductDetail(Long productId) {
        return productRepository.findProductDetailById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품 ID: " + productId));
    }

}
