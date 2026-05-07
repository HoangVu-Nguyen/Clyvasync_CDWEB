package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserWorkplace;
import com.profileservice.modules.profile.mapper.UserWorkplaceMapper;
import com.profileservice.modules.profile.mapstruct.WorkplaceMapper;
import com.profileservice.modules.profile.service.IWorkplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WorkplaceService implements IWorkplaceService {
    private final WorkplaceMapper workplaceMapper;
    private final UserWorkplaceMapper userWorkplaceMapper;
    @Override
    @Cached(name = "userWorkplaces:", key = "#userId", cacheType = CacheType.BOTH, expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<UserWorkplaceResponse> getWorkplacesByUserId(String userId) {
        return workplaceMapper.toUserWorkplaceResponse(userWorkplaceMapper.selectList(new LambdaQueryWrapper<UserWorkplace>().eq(UserWorkplace::getUserId,userId)));
    }
    //@CacheInvalidate(name = "userWorkplaces:", key = "#userId") nho xoa cache khi update
}
