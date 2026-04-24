package com.identityservice.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.identityservice.entity.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    @Select("SELECT r.name FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> findRoleNamesByUserId(String userId);

}
