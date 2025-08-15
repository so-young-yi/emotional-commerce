package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductV1ApiE2ETest {

    @Autowired TestRestTemplate testRestTemplate;
    @Autowired DatabaseCleanUp databaseCleanUp;

    @Autowired ProductRepository productRepository;
    @Autowired ProductStockRepository productStockRepository;
    @Autowired ProductMetaRepository productMetaRepository;
    @Autowired BrandRepository brandRepository;
    @Autowired UserRepository userRepository;

    private Long brandAId;
    private Long brandBId;
    private Long[] productIds = new Long[20];

    @BeforeEach
    void setup() {
        databaseCleanUp.truncateAllTables();

        // 브랜드 2개 생성
        BrandModel brandA = brandRepository.save(new BrandModel(null, "브랜드A", "브랜드A"));
        BrandModel brandB = brandRepository.save(new BrandModel(null, "브랜드B", "브랜드B"));
        brandAId = brandA.getId();
        brandBId = brandB.getId();

        // 상품 20개 생성 (브랜드 번갈아가며)
        for (int i = 0; i < 20; i++) {
            String productName = "상품" + (i + 1);
            Long brandId = (i % 2 == 0) ? brandAId : brandBId;
            ProductModel product = productRepository.save(
                    ProductModel.builder()
                            .brandId(brandId)
                            .name(productName)
                            .description(productName + " 설명")
                            .sellPrice(new com.loopers.support.Money((i + 1) * 1000L))
                            .status(ProductStatus.ON_SALE)
                            .sellAt(ZonedDateTime.now().minusDays(i))
                            .build()
            );
            productIds[i] = product.getId();

            // 재고/메타데이터 생성
            productStockRepository.save(ProductStockModel.builder()
                    .productId(product.getId())
                    .stock(10L + i)
                    .build());
            productMetaRepository.save(ProductMetaModel.builder()
                    .productId(product.getId())
                    .likeCount((long) (i + 1))
                    .reviewCount(0L)
                    .viewCount(0L)
                    .build());
        }
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("상품 목록: 기본(최근순, page=0, size=5) 페이징 정상")
    void success_list_defaultPaging() {
        String url = "/api/v1/products?page=0&size=5&sort=latest";
        ResponseEntity<ApiResponse<ProductV1Dto.ProductListPageResponse>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var body = response.getBody();
        assertThat(body).isNotNull();
        var data = body.data();
        assertThat(data.items()).hasSize(5);
    }

    @Test
    @DisplayName("상품 목록: 브랜드ID로 필터가 적용된다.")
    void success_list_withBrandFilter() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("brandId", brandAId)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Dto.ProductListPageResponse>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = response.getBody().data();
        assertThat(data.items()).isNotEmpty();
        assertThat(data.items().get(0).name()).contains("상품");
    }

    @Test
    @DisplayName("상품 목록: 낮은 가격순으로 정렬된다.")
    void success_list_sortsByLowPrice() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("sort", "price_asc")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Dto.ProductListPageResponse>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = response.getBody().data();
        assertAll(
                () -> assertThat(data.items()).hasSize(5),
                () -> assertThat(data.items().get(0).price()).isEqualTo(1000L)
        );
    }

    @Test
    @DisplayName("상품 목록: 좋아요 순으로 정렬된다.")
    void success_list_sortByLike() {
        String url = UriComponentsBuilder.fromUriString("/api/v1/products")
                .queryParam("sort", "likes_desc")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .build().toUriString();

        ResponseEntity<ApiResponse<ProductV1Dto.ProductListPageResponse>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = response.getBody().data();

        assertAll(
                () -> assertThat(data.items()).hasSize(5),
                () -> assertThat(data.items().get(0).likeCount()).isEqualTo(20L)
        );
    }

    @Test
    @DisplayName("상품 상세: 존재하는 상품이면 정상 응답한다.")
    void success_detail_found() {
        Long existingProductId = productIds[0];

        ResponseEntity<ApiResponse<ProductV1Dto.ProductDetailResponse>> response = testRestTemplate.exchange(
                "/api/v1/products/" + existingProductId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var detail = response.getBody().data();

        assertAll(
                () -> assertThat(detail.name()).contains("상품"),
                () -> assertThat(detail.price()).isNotNull(),
                () -> assertThat(detail.likeCount()).isNotNull()
        );
    }

    @Test
    @DisplayName("상품 상세: 존재하지 않는 상품이면 404")
    void failure_detail_notFound() {
        Long nonExistentProductId = 99999L;

        ResponseEntity<ApiResponse> response = testRestTemplate.getForEntity("/api/v1/products/" + nonExistentProductId, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().meta().message()).contains("존재하지 않는 상품 ID: " + nonExistentProductId);
    }
}
