package com.identityservice.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.identityservice.entity.auth.entity.UserCredential;
import com.identityservice.mapper.auth.UserCredentialMapper;
import com.identityservice.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.baomidou.mybatisplus.extension.toolkit.Db.getOne;
@Service
@RequiredArgsConstructor
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
}