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

    public ProductModel getProductDetail( Long productId ) {
        return productRepository.findById( productId )
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
    }

    public Page<ProductModel> getProducts( ProductSearchCriteria criteria ) {

        int page = criteria.page() != null ? criteria.page() : 0;
        int size = criteria.size() != null ? criteria.size() : 20;
        String sort = criteria.sort() != null ? criteria.sort() : "latest";

        Sort sortObj = switch (sort) {
            case "price_asc" -> Sort.by("sellPrice.amount").ascending();
            // case "likes_desc" -> ... // 좋아요순 정렬은 파사드에서 처리
            default -> Sort.by("sellAt").descending(); // 최신순: 판매일자 기준
        };

        PageRequest pageRequest = PageRequest.of(page, size, sortObj);

        if (criteria.brandId() != null) {
            return productRepository.findByBrandId(criteria.brandId(), pageRequest);
        }
        else {
            return productRepository.findAll(pageRequest);
        }
    }

}
