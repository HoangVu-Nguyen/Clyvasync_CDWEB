package com.profileservice.modules.profile.mapstruct;

import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserWorkplace;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkplaceMapper {
    UserWorkplaceResponse toUserWorkplaceResponse(UserWorkplace userWorkplace);
    List<UserWorkplaceResponse> toUserWorkplaceResponse(List<UserWorkplace> userWorkplace);
    UserWorkplace toUserWorkplace(UserWorkplaceResponse userWorkplaceResponse);
    List<UserWorkplace> toUserWorkplace(List<UserWorkplaceResponse> userWorkplaceResponse);
}
