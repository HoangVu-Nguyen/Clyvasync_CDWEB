package com.identityservice.service.auth.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.identityservice.entity.auth.entity.Role;
import com.identityservice.entity.auth.entity.UserRole;
import com.identityservice.mapper.auth.RoleMapper;
import com.identityservice.mapper.auth.UserRoleMapper;
import com.identityservice.service.auth.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleMapper userRoleMapper;

    @Override
    public Set<String> findRolesByUserId(String userId) {
        return userRoleMapper.findRoleNamesByUserId(userId);
    }
}
