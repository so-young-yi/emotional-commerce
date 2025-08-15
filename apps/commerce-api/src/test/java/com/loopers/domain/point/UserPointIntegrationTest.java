package com.loopers.domain.point;

import com.loopers.application.point.UserPointFacade;
import com.loopers.application.point.UserPointInfo;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 포인트 조회 통합 테스트
@SpringBootTest
public class UserPointIntegrationTest {

    @DisplayName("포인트 조회할때, ")
    @Nested
    class PointGet{

        @Autowired
        UserRepository userRepository;

        @Autowired
        UserPointRepository userPointRepository;

        @Autowired
        private UserPointFacade userPointFacade;

        Long testUserId;
        Long testUserPoint;

        @BeforeEach
        void setUp() {
            userRepository.save(new UserModel("riley",
                                                "riley",
                                                "riley@test.com",
                                                "2000-01-01",
                                                Gender.F));
            UserPointModel save = userPointRepository.save(new UserPointModel(1L,1000L));
            testUserPoint = save.getBalance();
            testUserId = save.getUserId();
        }

        @DisplayName("회원 존재 시 보유 포인트 반환")
        @Test
        public void shouldReturnPointsWhenUserExists() {

            // arrange
            // act
            UserPointInfo getPoint = userPointFacade.getUserPoint(testUserId);

            // assert
            assertAll(
                    () -> assertThat(getPoint).isNotNull(),
                    () -> assertThat(getPoint.point()).isGreaterThanOrEqualTo(0),
                    () -> assertThat(getPoint.point()).isEqualTo(testUserPoint)
            );

        }

        @DisplayName("회원 존재하지 않으면 예외 발생")
        @Test
        public void shouldThrowExceptionWhenUserDoesNotExist() {
            // arrange
            Long invalidUserId = 9999999L;

            // act & assert
            assertThrows(CoreException.class, () -> {
                userPointFacade.getUserPoint(invalidUserId);
            });
        }
    }

}
