package com.identityservice.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.identityservice.entity.auth.entity.UserCredential;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredential> {
}