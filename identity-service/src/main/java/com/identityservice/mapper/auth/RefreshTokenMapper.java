package com.identityservice.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.identityservice.entity.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}