package com.profileservice.modules.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.profileservice.modules.photo.entity.profile.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
