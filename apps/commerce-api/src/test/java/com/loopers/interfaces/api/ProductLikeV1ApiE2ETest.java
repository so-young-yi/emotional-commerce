package com.loopers.interfaces.api;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

// ProductLikeV1ApiE2ETest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductLikeV1ApiE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("POST /api/v1/products/{productId}/likes")
    class LikeProduct {

        String ENDPOINT = "/api/v1/products/{productId}/likes";

        @Test
        @DisplayName("상품 좋아요 등록 성공")
        void likeProduct_whenNotLikedYet() {

            //arr
            long userId = 1L;
            long productId = 1L;
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", String.valueOf(userId));
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            //act
            ParameterizedTypeReference<ApiResponse<Boolean>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(ENDPOINT, HttpMethod.POST, httpEntity, responseType, productId);

            //assert
            assertThat(response.getBody().data()).isEqualTo(true);
        }

        @Test
        @DisplayName("미로그인 시 400 반환")
        void return401_whenNotLoggedIn() {

            //arr
            long productId = 1L;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            //act
            ParameterizedTypeReference<ApiResponse<Boolean>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(ENDPOINT, HttpMethod.POST, httpEntity, responseType, productId);
            System.out.println(response);

            //assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productId}/likes")
    class UnlikeProduct {
        String ENDPOINT = "/api/v1/products/{productId}/likes";

        @Test
        @DisplayName("상품 좋아요 해제 성공")
        void unlikeProduct_whenLiked() {

            //arr
            long userId = 1L;
            long productId = 1L;
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", String.valueOf(userId));
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            //act
            ParameterizedTypeReference<ApiResponse<Boolean>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(ENDPOINT, HttpMethod.DELETE, httpEntity, responseType, productId);

            //assert
            assertThat(response.getBody().data()).isEqualTo(false);
        }
    }
}
