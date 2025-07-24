package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 회원 가입 단위 테스트
public class UserUnitTest {

    @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않을 경우")
    @ParameterizedTest
    @ValueSource(strings = {
            "riley----------",
            "rrrrrrill111",
            "Adgashdlgkdsdgd",
            "12343254353assgsdgsdg"
    })
    public void shouldFailWhenIdIsInvalid(String userId) throws CoreException {

        // arrange
        String name = "riley";
        String mail = "test@email.com";
        String birth = "2000-01-01";
        Gender gender = Gender.F;
        // act
        CoreException exception = assertThrows(CoreException.class, () -> {

            new UserModel(
                    userId,
                    name,
                    mail,
                    birth,
                    gender
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않을 경우")
    @ParameterizedTest
    @ValueSource(strings = {
            "riley",
            "rrrrrrill111",
            "Adgashdlgkdsdgd",
            "12343254353assgsdgsdg"
    })
    public void shouldFailWhenEmailIsInvalid( String email ) {

        // arrange
        String userId = "riley1234";
        String name = "riley";
        String birth = "2000-01-01";
        Gender gender = Gender.F;
        // act
        CoreException exception = assertThrows(CoreException.class, () -> {
            new UserModel(
                    userId,
                    name,
                    email,
                    birth,
                    gender
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않을 경우")
    @ParameterizedTest
    @ValueSource(strings = {
            "1/1",
            "0101",
            "20000101",
            "2000/01/01"
    })
    public void shouldFailWhenBirthDateIsInvalid(String birth) {

        // arrange
        String userId = "riley1234";
        String name = "riley";
        String email = "riley@email.com";
        Gender gender = Gender.F;
        // act
        CoreException exception = assertThrows(CoreException.class, () -> {
            new UserModel(
                    userId,
                    name,
                    email,
                    birth,
                    gender
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }


    @DisplayName("invalidGender를 검증하는 테스트코드")
    @ParameterizedTest
    @ValueSource(strings={"INVALID","MALEE","FEMALL","123"})
    public void shouldFail_whenGenderIsInvalid(String invalidGender){

        // arrange
        // act & assert
        assertThrows(IllegalArgumentException.class, ()->{
            Gender.valueOf(invalidGender);
        });
    }

    @DisplayName("gender를 검증하는 테스트코드")
    @ParameterizedTest
    @ValueSource(strings={"M","F"})
    public void shouldSuccess_whenGenderIsValid(String genderString){

        // arrange
        String userId = "riley1234";
        String name = "riley";
        String email = "riley@email.com";
        String birth = "2000-01-01";
        Gender gender = Gender.valueOf(genderString);

        // act
        UserModel userModel = new UserModel(userId, name, email, birth, gender);

        // assert
        assertThat(userModel.getGender()).isEqualTo(gender);
    }

}
