package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductMetaService {

    private final ProductMetaRepository productMetaRepository;

    public ProductMetaModel getMeta(Long productId) {
        return productMetaRepository.findByProductId(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 메타정보가 존재하지 않습니다."));
    }

    public Map<Long, ProductMetaModel> getMetasForProductIds(List<Long> productIds) {
        List<ProductMetaModel> metas = productMetaRepository.findByProductIdIn(productIds);
        return metas.stream().collect(Collectors.toMap(ProductMetaModel::getProductId, m -> m));
    }

    @Transactional
    public ProductMetaModel save(ProductMetaModel meta) {
        return productMetaRepository.save(meta);
    }

    @Transactional
    public void increaseLike(Long productId) {
        ProductMetaModel meta = productMetaRepository.findByProductId(productId)
                .orElse(ProductMetaModel.builder()
                        .productId(productId)
                        .likeCount(0L)
                        .reviewCount(0L)
                        .viewCount(0L)
                        .build());
        meta.increaseLike();
        productMetaRepository.save(meta);
    }

    @Transactional
    public void decreaseLike(Long productId) {
        ProductMetaModel meta = productMetaRepository.findByProductId(productId)
                .orElse(ProductMetaModel.builder()
                        .productId(productId)
                        .likeCount(0L)
                        .reviewCount(0L)
                        .viewCount(0L)
                        .build());
        meta.decreaseLike();
        productMetaRepository.save(meta);
    }

}
