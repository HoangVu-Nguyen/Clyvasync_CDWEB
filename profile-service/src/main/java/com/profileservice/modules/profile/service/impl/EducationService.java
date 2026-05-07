package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import com.profileservice.modules.profile.mapper.UserEducationMapper;
import com.profileservice.modules.profile.mapstruct.EducationMapper;
import com.profileservice.modules.profile.service.IEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EducationService implements IEducationService {
    private final UserEducationMapper userEducationMapper;
    private final EducationMapper educationMapper;

    @Override
    @Cached(name = "userEducations:", key = "#userId", cacheType = CacheType.BOTH, expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<UserEducationResponse> getEducationsByUserId(String userId) {
        return educationMapper.toUserEducationResponse(userEducationMapper.selectList(new LambdaQueryWrapper<UserEducation>().eq(UserEducation::getUserId,userId)));
    }
    //@CacheInvalidate(name = "userEducations:", key = "#userId") nho xoa cache khi update
}
