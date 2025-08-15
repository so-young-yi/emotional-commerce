package com.loopers.domain.product;

import com.loopers.support.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Nested
    @DisplayName("상품 생성/조회 통합 테스트")
    class CreateAndFindProduct {

        @Test
        @DisplayName("상품을 생성하면 DB에 저장되고, 조회할 수 있다")
        void createProduct_shouldSaveAndFind() {
            // arrange
            ProductModel product = new ProductModel(
                    null, // id는 auto-generated
                    1L,
                    "에어맥스",
                    "에어맥스 설명",
                    new Money(100000L),
                    10L,
                    ProductStatus.ON_SALE,
                    java.time.ZonedDateTime.now()
            );

            // act
            ProductModel saved = productRepository.save(product);

            // assert
            ProductModel found = productRepository.findById(saved.getId()).orElse(null);
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("에어맥스");
            assertThat(found.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        }
    }

    @Nested
    @DisplayName("상품 목록/정렬 통합 테스트")
    class FindProductList {

        @Test
        @DisplayName("브랜드별 상품 목록을 조회할 수 있다")
        void findByBrandId_shouldReturnProductList() {
            // arrange
            Long brandId = 1L;
            ProductModel product1 = new ProductModel(null, brandId, "에어맥스", "설명", new Money(100000L), 10L, ProductStatus.ON_SALE, java.time.ZonedDateTime.now());
            ProductModel product2 = new ProductModel(null, brandId, "에어포스", "설명", new Money(120000L), 5L, ProductStatus.ON_SALE, java.time.ZonedDateTime.now());
            productRepository.save(product1);
            productRepository.save(product2);

            // act
            var page = productRepository.findByBrandId(brandId, org.springframework.data.domain.PageRequest.of(0, 10));

            // assert
            assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("상품 재고 차감 통합 테스트")
    class DecreaseStock {

        @Test
        @DisplayName("상품 재고를 차감하면 DB에 반영된다")
        void decreaseStock_shouldUpdateStock() {
            // arrange
            ProductModel product = new ProductModel(null, 1L, "에어맥스", "설명", new Money(100000L), 10L, ProductStatus.ON_SALE, java.time.ZonedDateTime.now());
            ProductModel saved = productRepository.save(product);

            // act
            saved.decreaseStock(3L);
            productRepository.save(saved);

            // assert
            ProductModel found = productRepository.findById(saved.getId()).orElse(null);
            assertThat(found.getStock()).isEqualTo(7L);
        }
    }
}
