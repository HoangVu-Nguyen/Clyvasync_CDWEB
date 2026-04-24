package com.identityservice.service.auth.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commonlibrary.exception.AppException;
import com.commonlibrary.exception.ResultCode;
import com.identityservice.entity.auth.entity.Role;
import com.identityservice.entity.auth.entity.UserRole;
import com.identityservice.enums.auth.RoleName;
import com.identityservice.mapper.auth.RoleMapper;
import com.identityservice.mapper.auth.UserRoleMapper;
import com.identityservice.service.auth.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public Set<String> findRolesByUserId(String userId) {
        return userRoleMapper.findRoleNamesByUserId(userId);
    }
    @Override
    @Transactional // Bắt buộc có Transaction để đảm bảo tính toàn vẹn
    public void assignDefaultRole(String userId, RoleName roleName) {
        log.info("Đang gán role {} cho user ID: {}", roleName, userId);

        // 1. Tìm Role trong DB dựa trên Enum RoleName
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, roleName));

        if (role == null) {
            log.error("Không tìm thấy Role {} trong hệ thống!", roleName);
            throw new AppException(ResultCode.ROLE_NOT_FOUND);
        }

        // 2. Chèn vào bảng trung gian user_roles
        // Chúng ta dùng một Entity đơn giản hoặc dùng Map/SQL tùy ý
        try {
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(role.getId())
                    .build();

            userRoleMapper.insert(userRole);
        } catch (Exception e) {
            log.error("Lỗi khi gán role: {}", e.getMessage());
            // Tránh lỗi duplicate nếu user đã có role này rồi
            throw new AppException(ResultCode.FAILED);
        }
    }
}
