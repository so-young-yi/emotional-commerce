package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    public UserModel signup(UserModel user) {
        if (userRepository.findByUserId(user.getUserId()).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 가입된 아이디입니다.");
        }
        return userRepository.save(user);
    }

    public UserModel getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public UserModel getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[userId = " + userId + "] 사용자를 찾을 수 없습니다."));
    }



}
