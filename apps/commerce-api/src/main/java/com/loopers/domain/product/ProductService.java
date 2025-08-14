package com.loopers.domain.product;

import com.loopers.application.product.ProductSearchCriteria;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductSummaryProjection> getProductSummaries(ProductSearchCriteria criteria) {
        int page = criteria.getPageOrDefault();
        int size = criteria.getSizeOrDefault();
        ProductSortType sortType = criteria.getSortTypeOrDefault();
        Sort sortObj = Sort.by(sortType.getDirection(), sortType.getSortField());
        PageRequest pageRequest = PageRequest.of(page, size, sortObj);
        return productRepository.findProductSummaries(criteria.brandId(), pageRequest);
    }

    public ProductDetailProjection getProductDetail(Long productId) {
        return productRepository.findProductDetailById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품 ID: " + productId));
    }

}
