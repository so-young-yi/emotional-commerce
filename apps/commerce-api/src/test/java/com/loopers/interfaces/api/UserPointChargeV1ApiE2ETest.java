package com.loopers.interfaces.api;

import com.loopers.interfaces.api.point.UserPointV1Dto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

// 포인트 충전 E2E 테스트
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserPointChargeV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class ChargePoint {

        private static final String ENDPOINT = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        @Sql(statements = {
                "INSERT INTO member (login_id, name, email, birth, gender, created_at, updated_at, deleted_at) VALUES ('riley', 'riley', 'riley@test.com', '2000-01-01', 'F', NOW(), NOW(), NULL)",
                "INSERT INTO user_point (user_id, balance) VALUES (1, 0)"
        })

        void returnTotalAmount_whenChargeSuccess() {
            // arrange
            Long userId = 1L;
            Long chargeAmount = 1000L;

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", userId.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);

            UserPointV1Dto.PointChargeRequest request = new UserPointV1Dto.PointChargeRequest(chargeAmount);
            HttpEntity<UserPointV1Dto.PointChargeRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserPointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserPointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data().point()).isGreaterThanOrEqualTo(chargeAmount)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void return404_whenUserDoesNotExist() {
            // arrange
            Long nonExistentUserId = 9999L;
            Long chargeAmount = 1000L;

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", nonExistentUserId.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);

            UserPointV1Dto.PointChargeRequest request = new UserPointV1Dto.PointChargeRequest(chargeAmount);
            HttpEntity<UserPointV1Dto.PointChargeRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserPointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserPointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
