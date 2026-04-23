package com.identityservice.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.identityservice.entity.auth.entity.UserDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDevice> {
}
