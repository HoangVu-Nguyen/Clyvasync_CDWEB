package com.profileservice.modules.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.profileservice.modules.profile.entity.profile.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
