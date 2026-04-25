package com.identityservice.security.custom;




import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.identityservice.entity.auth.entity.UserCredential;
import com.identityservice.mapper.auth.UserCredentialMapper;
import com.identityservice.service.auth.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j // Thêm cái này để dùng biến 'log'
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialMapper userMapper;
    // Giả sử ông có service lấy roles
    private final UserRoleService userRoleService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user bằng MyBatis-Plus
        UserCredential user = userMapper.selectOne(new LambdaQueryWrapper<UserCredential>()
                .eq(UserCredential::getEmail, email));

        if (user == null) {
            throw new AppException(ResultCode.USER_NOT_FOUND);
        }

        Set<String> roles = userRoleService.findRolesByUserId(user.getId());

        // 3. Trả về CustomUserDetails
        return new CustomUserDetails(user, roles);
    }
}