package com.loopers.application.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;

public record UserInfo(Long id,
                       String userId,
                       String userName,
                       String email,
                       String birth,
                       Gender gender
                       ) {

    public static UserInfo from(UserModel user) {
        return new UserInfo(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getBirth(),
                user.getGender()
        );
    }

    public static UserModel to(UserInfo user) {
        return new UserModel(
                user.userId(),
                user.userName(),
                user.email(),
                user.birth(),
                user.gender()
        );
    }
}
