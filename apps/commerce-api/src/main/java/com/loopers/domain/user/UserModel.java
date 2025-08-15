package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class UserModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;
    private String name;
    private String email;
    private String birth;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 형식 검증 regx
    private final String PATTERN_USER_ID = "^[a-zA-Z0-9]{1,10}$";
    private final String PATTERN_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String PATTERN_BIRTH = "^\\d{4}-\\d{2}-\\d{2}$";

    public UserModel(
            String loginId,
            String name,
            String email,
            String birth,
            Gender gender
    ){
        if ( loginId == null || !loginId.matches(PATTERN_USER_ID) ) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "ID는 영문 및 숫자 10자 이내로 입력해야 합니다."
            );
        }
        if ( email == null || !email.matches(PATTERN_EMAIL) ) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "이메일은 xx@yy.zz 형식이어야 합니다."
            );
        }
        if ( birth == null || !birth.matches(PATTERN_BIRTH) ) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "생년월일은 yyyy-MM-dd 형식이어야 합니다."
            );
        }
        if ( gender == null ) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "성별은 필수입니다."
            );
        }
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.birth = birth;
        this.gender = gender;
    }

}
