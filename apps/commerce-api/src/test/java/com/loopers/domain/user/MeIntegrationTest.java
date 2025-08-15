package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 내 정보 조회 통합 테스트
@SpringBootTest
public class MeIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("내 정보를 조회할때,")
    @Nested
    public class Me {

        @DisplayName("해당 ID 회원 존재 시 정보 반환")
        @Test
        public void shouldReturnUserInfoWhenUserExists() {

            // arrange
            UserModel userModel = userJpaRepository.save(new UserModel("riley",
                    "riley",
                    "riley@test.com",
                    "2000-01-01",
                    Gender.F));

            // act
            UserModel result = userService.getUserByLoginId(userModel.getLoginId());

            // assert
            assertAll(
                    ()-> assertThat(result).isNotNull(),
                    ()-> assertThat(result.getLoginId()).isEqualTo(userModel.getLoginId()),
                    ()-> assertThat(result.getName()).isEqualTo(userModel.getName()),
                    ()-> assertThat(result.getEmail()).isEqualTo(userModel.getEmail())
            );
        }

        @DisplayName("해당 ID 회원이 없으면 예외 발생")
        @Test
        public void shouldThrowExceptionWhenUserDoesNotExist() {

            // arrange
            Long invalidUserId = 99L;

            // act & assert
            assertThrows(CoreException.class, () -> {
                userService.getUserById(invalidUserId);
            });
        }
    }
}
