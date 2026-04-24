package com.identityservice.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.identityservice.entity.auth.entity.UserCredential;
import com.identityservice.mapper.auth.UserCredentialMapper;
import com.identityservice.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.baomidou.mybatisplus.extension.toolkit.Db.getOne;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserCredentialMapper userCredentialMapper;

    @Override
    public Optional<UserCredential> findOptionalByEmail(String email) {
        UserCredential user = userCredentialMapper.selectOne(
                new LambdaQueryWrapper<UserCredential>()
                        .eq(UserCredential::getEmail, email)
        );
        return Optional.ofNullable(user);
    }
    @Override
    @Transactional
    public UserCredential save(UserCredential user) {
        // Nếu user chưa có ID, MyBatis-Plus sẽ tự dùng Snowflake (ASSIGN_ID) để sinh ID
        if (user.getId() == null) {
            userCredentialMapper.insert(user);
            log.info("Đã tạo user mới với ID: {}", user.getId());
        } else {
            userCredentialMapper.updateById(user);
            log.info("Đã cập nhật thông tin cho user ID: {}", user.getId());
        }
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userCredentialMapper.exists(
                new LambdaQueryWrapper<UserCredential>()
                        .eq(UserCredential::getEmail, email)
        );
    }
}