package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductStockService {

    private final ProductStockRepository productStockRepository;

    /**
     * 상품 재고 조회
     */
    public ProductStockModel getStock(Long productId) {
        return productStockRepository.findByProductId(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 재고정보가 존재하지 않습니다."));
    }

    /**
     * 재고 증가 (입고 등)
     */
    @CacheEvict(cacheNames = "product:list", allEntries = true)
    @Transactional
    public void increaseStockWithLock(Long productId, long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "증가 수량은 1 이상이어야 합니다.");
        ProductStockModel stock = productStockRepository.findByProductIdForUpdate(productId)
                .orElse(ProductStockModel.builder().productId(productId).stock(0L).build());
        stock.increaseStock(qty);
        productStockRepository.save(stock);
    }

    /**
     * 재고 감소 (주문 등)
     */
    @CacheEvict(cacheNames = "product:list", allEntries = true)
    @Transactional
    public void decreaseStockWithLock(Long productId, long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 1 이상이어야 합니다.");
        ProductStockModel stock = productStockRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 재고정보가 존재하지 않습니다."));
        stock.decreaseStock(qty);
        productStockRepository.save(stock);
    }

    /**
     * 재고가 충분한지 체크
     */
    public boolean hasEnoughStock(Long productId, long qty) {
        ProductStockModel stock = productStockRepository.findByProductId(productId)
                .orElse(null);
        return stock != null && stock.getStock() >= qty;
    }

    public Map<Long, ProductStockModel> getStocksForProductIds(List<Long> productIds) {
        List<ProductStockModel> stocks = productStockRepository.findByProductIdIn(productIds);
        return stocks.stream().collect(Collectors.toMap(ProductStockModel::getProductId, s -> s));
    }
}
