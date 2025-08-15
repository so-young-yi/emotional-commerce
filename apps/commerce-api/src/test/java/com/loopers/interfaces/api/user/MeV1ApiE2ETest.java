package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.user.UserJpaRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 내 정보 조회 E2E 테스트
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MeV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/me")
    @Nested
    class Me {

        private UserModel saved;
        @BeforeEach
        void setUp() {
             saved = userJpaRepository.save(
                    new UserModel(
                            "riley",
                            "riley",
                            "riley@test.com",
                            "2000-01-01",
                            Gender.F
                    )
            );
        }

        private static final String ENDPOINT = "/api/v1/me/{id}";

        @DisplayName("내 정보 조회 성공 시 유저 정보 반환")
        @Test
        public void shouldReturnUserInfoWhenGetMyInfoSuccess() {

            // arrange
            final Long id = saved.getId();

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, null, responseType, id);

            // assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

        }

        @DisplayName("존재하지 않는 ID 조회 시 404 Not Found")
        @Test
        public void shouldReturn404WhenUserNotFound() {

            // arrange
            final Long id = 100L;

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, null, responseType, id);

            // assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        }
    }

}
