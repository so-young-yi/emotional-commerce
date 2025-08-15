package com.loopers.application.product;

import com.loopers.application.like.ProductLikeInfo;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.*;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.ZonedDateTime;

@SpringBootTest
class ProductServiceCacheTest {

    @MockitoSpyBean ProductRepository productRepository;
    @Autowired BrandRepository brandRepository;
    @Autowired ProductStockRepository productStockRepository;
    @Autowired ProductMetaRepository productMetaRepository;
    @Autowired ProductService productService;
    @Autowired ProductLikeService productLikeService;
    @Autowired RedisCleanUp redisCleanUp;
    @AfterEach void tearDown() {
        redisCleanUp.truncateAll();
    }


    @BeforeEach
    void setup() {
        // 1. 브랜드 생성
        BrandModel brand = brandRepository.save(
                BrandModel.builder()
                        .name("브랜드1")
                        .description("브랜드1 설명")
                        .build()
        );

        // 2. 상품 10개 생성 (brandId=1)
        for (int i = 1; i <= 10; i++) {
            ProductModel product = productRepository.save(
                    ProductModel.builder()
                            .brandId(brand.getId())
                            .name("상품" + i)
                            .description("상품" + i + " 설명")
                            .sellPrice(new com.loopers.support.Money(1000L * i))
                            .status(ProductStatus.ON_SALE)
                            .sellAt(ZonedDateTime.now())
                            .build()
            );

            // 3. 재고 생성
            productStockRepository.save(
                    ProductStockModel.builder()
                            .productId(product.getId())
                            .stock(10L)
                            .build()
            );

            // 4. 메타(좋아요 등) 생성
            productMetaRepository.save(
                    ProductMetaModel.builder()
                            .productId(product.getId())
                            .likeCount((long) i)
                            .reviewCount(0L)
                            .viewCount(0L)
                            .build()
            );
        }
    }

    @Nested
    @DisplayName("상품 목록 캐시")
    class ProductListCache {

        @Test
        @DisplayName("캐시 적중 시 DB는 1번만 호출된다")
        void 상품목록_캐시_적중_테스트() {
            ProductSearchCriteria criteria = new ProductSearchCriteria(1L, ProductSortType.LATEST, 0, 10);

            productService.getProductSummaries(criteria); // 1회 (MISS)
            productService.getProductSummaries(criteria); // 2회 (HIT)

            Mockito.verify(productRepository, Mockito.times(1))
                    .findProductSummaries(Mockito.any(), Mockito.any());
        }

        @Test
        @DisplayName("캐시 무효화(@CacheEvict) 후 DB가 다시 호출된다")
        void 상품목록_캐시_무효화_테스트() {
            ProductSearchCriteria criteria = new ProductSearchCriteria(1L, ProductSortType.LATEST, 0, 10);

            productService.getProductSummaries(criteria); // 1회 (MISS)
            // 좋아요 등 캐시 무효화 메서드 호출
            productLikeService.likeProduct(new ProductLikeInfo(1L, 1L)); // 예시
            productService.getProductSummaries(criteria); // 2회 (Evict → MISS)
            Mockito.verify(productRepository, Mockito.times(2))
                    .findProductSummaries(Mockito.any(), Mockito.any());
        }
    }
}
