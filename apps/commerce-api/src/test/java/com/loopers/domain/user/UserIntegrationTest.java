package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 회원 가입 통합 테스트
@SpringBootTest
public class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {databaseCleanUp.truncateAllTables();}

    @DisplayName("회원 가입할떄, ")
    @Nested
    class SignUp{

        @DisplayName("회원 가입시 User 저장이 수행됨 (spy 검증)")
        @Test
        public void shouldSaveUserWhenSignUp() {

            // arrange
            UserModel userModel = new UserModel(
                    "riley",
                    "riley",
                    "riley@test.com",
                    "2000-01-01",
                    Gender.F
            );

            // act
            UserModel result = userService.signup(userModel);

            // assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getLoginId()).isEqualTo(userModel.getLoginId()),
                    () -> assertThat(result.getName()).isEqualTo(userModel.getName()),
                    () -> assertThat(result.getBirth()).isEqualTo(userModel.getBirth())
            );
            verify(userJpaRepository, times(1)).save(any(UserModel.class));

        }

        @DisplayName("이미 가입된 ID로 회원가입 시도 시 실패")
        @Test
        public void shouldFailWhenSignUpWithDuplicateId() {

            // arrange
            UserModel existingUser = new UserModel(
                    "riley",
                    "riley",
                    "riley@test.com",
                    "2000-01-01",
                    Gender.F
            );
            userService.signup(existingUser);

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userService.signup(existingUser);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }

    }
}
