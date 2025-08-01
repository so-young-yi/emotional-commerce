package com.loopers.domain.point;

import com.loopers.application.point.UserPointFacade;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 포인트 충전 통합 테스트
@SpringBootTest
public class UserPointChargeIntegrationTest {

    @DisplayName("포인트 충전할때, ")
    @Nested
    class PointCharge{


        @Autowired
        private UserPointFacade userPointFacade;

        @DisplayName("존재하지 않는 유저 ID 충전 시 실패")
        @Test
        public void shouldFailWhenUserDoesNotExist() {

            // arrange
            Long invalidId = 999999L;
            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userPointFacade.chargeUserPoint(invalidId, 100000L);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }
    }

}
