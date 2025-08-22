package com.loopers.interfaces.api.order;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.Money;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderPaymentV1ApiE2ETest {

    public static class Fixtures {

        public static BrandModel brand(String name) {
            return BrandModel.builder()
                    .name(name)
                    .description(name + " 설명")
                    .build();
        }

        public static UserModel user(String loginId, String name, Gender gender) {
            return new UserModel(loginId, name, loginId + "@test.com", "2000-01-01", gender);
        }

        public static ProductModel product(Long brandId, String name, long price) {
            return ProductModel.builder()
                    .brandId(brandId)
                    .name(name)
                    .description(name + " 설명")
                    .sellPrice(new Money(price))
                    .status(ProductStatus.ON_SALE)
                    .sellAt(ZonedDateTime.now())
                    .build();
        }

        public static ProductStockModel stock(Long productId, long stock) {
            return ProductStockModel.builder()
                    .productId(productId)
                    .stock(stock)
                    .build();
        }

        public static ProductMetaModel meta(Long productId, long likeCount) {
            return ProductMetaModel.builder()
                    .productId(productId)
                    .likeCount(likeCount)
                    .reviewCount(0L)
                    .viewCount(0L)
                    .build();
        }
    }

    @Autowired TestRestTemplate testRestTemplate;
    @Autowired UserRepository userRepository;
    @Autowired UserPointRepository userPointRepository;
    @Autowired BrandRepository brandRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductStockRepository productStockRepository;
    @Autowired ProductMetaRepository productMetaRepository;

    @Autowired DatabaseCleanUp databaseCleanUp;
    @AfterEach void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @BeforeEach
    void setup() {

        UserModel user = userRepository.save(Fixtures.user("riley", "riley", Gender.F));
        userPointRepository.save(new UserPointModel(user.getId(), 10000L));

        BrandModel brandA = brandRepository.save(Fixtures.brand("브랜드A"));
        BrandModel brandB = brandRepository.save(Fixtures.brand("브랜드B"));

        for (int i = 0; i < 10; i++) {
            String productName = "상품" + (i + 1);
            Long brandId = (i % 2 == 0) ? brandA.getId() : brandB.getId();
            ProductModel product = productRepository.save(Fixtures.product(brandId, productName, (i + 1) * 1000L));
            productStockRepository.save(Fixtures.stock(product.getId(), 2L + i));
            productMetaRepository.save(Fixtures.meta(product.getId(), i + 1));
        }
    }

    @Test
    @DisplayName("재고 부족 시 주문 실패")
    void order_fail_when_stock_not_enough() {
        long userId = 1L;
        long productId = 1L;
        long orderQty = 5L;

        OrderV1Dto.OrderRequest orderRequest = new OrderV1Dto.OrderRequest(
                List.of(new OrderV1Dto.OrderItem(productId, orderQty, null)), null
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-USER-ID", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderV1Dto.OrderRequest> httpEntity = new HttpEntity<>(orderRequest, headers);

        ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<ApiResponse<OrderV1Dto.OrderResponse>> response =
                testRestTemplate.exchange("/api/v1/orders", HttpMethod.POST, httpEntity, responseType);

        // 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().meta().message()).contains("재고");
    }
}
