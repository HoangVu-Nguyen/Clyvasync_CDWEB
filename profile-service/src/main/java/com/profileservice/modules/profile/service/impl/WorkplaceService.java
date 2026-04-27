package com.profileservice.modules.profile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserWorkplace;
import com.profileservice.modules.profile.mapper.UserWorkplaceMapper;
import com.profileservice.modules.profile.mapstruct.WorkplaceMapper;
import com.profileservice.modules.profile.service.IWorkplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class WorkplaceService implements IWorkplaceService {
    private final WorkplaceMapper workplaceMapper;
    private final UserWorkplaceMapper userWorkplaceMapper;
    @Override
    public List<UserWorkplaceResponse> getWorkplacesByUserId(String userId) {
        return workplaceMapper.toUserWorkplaceResponse(userWorkplaceMapper.selectList(new LambdaQueryWrapper<UserWorkplace>().eq(UserWorkplace::getUserId,userId)));
    }
}
