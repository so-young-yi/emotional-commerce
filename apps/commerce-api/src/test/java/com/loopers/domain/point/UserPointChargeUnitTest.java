package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 포인트 충전 단위 테스트
public class UserPointChargeUnitTest {

    @DisplayName("0 이하 정수 충전 시 실패")
    @ParameterizedTest
    @ValueSource(ints = {
            0,
            -1,
            -10,
            -100
    })
    public void shouldFailWhenChargeAmountIsZeroOrNegative(int chargeAmount) {

        // arrange
        // act
        CoreException exception = assertThrows(CoreException.class, () -> {
            new UserPointModel(1L, 0).charge(chargeAmount);
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
