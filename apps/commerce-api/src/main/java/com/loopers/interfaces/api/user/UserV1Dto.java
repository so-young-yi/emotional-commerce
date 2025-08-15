package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {


    public record SignUpRequest(
            @NotNull
            String userId,
            @NotNull
            String name,
            @NotNull
            String email,
            @NotNull
            String birth,
            @NotNull
            GenderResponse gender
    ){
        public UserModel toModel() {
            if (gender == null) {
                throw new CoreException (ErrorType.BAD_REQUEST,
                                        "성별은 필수 입력값입니다." );
            }
            return new UserModel(
                    userId,
                    name,
                    email,
                    birth,
                    gender.toModel()
            );
        }
    }

    public record UserResponse (
            String userId,
            String name,
            String email,
            String birth,
            GenderResponse gender
    ){
        public static UserResponse from(UserInfo info) {
            return new UserResponse(
                    info.userId(),
                    info.userName(),
                    info.email(),
                    info.birth(),
                    GenderResponse.valueOf(info.gender().name())
            );
        }
    }

    public enum GenderResponse {
        M,
        F;

        public Gender toModel() {
            return Gender.valueOf(this.name());
        }

    }
}
