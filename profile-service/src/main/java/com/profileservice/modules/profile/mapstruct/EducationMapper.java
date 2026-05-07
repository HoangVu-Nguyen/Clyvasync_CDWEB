package com.profileservice.modules.profile.mapstruct;

import com.profileservice.modules.profile.dto.request.UserEducationRequest;
import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EducationMapper {
    UserEducationResponse toUserEducationResponse(UserEducation userEducation);
    List<UserEducationResponse> toUserEducationResponse(List<UserEducation> userEducation);
    UserEducation toUserEducation(UserEducationResponse userEducationResponse);
    List<UserEducation> toUserEducation(List<UserEducationResponse> userEducationResponse);
    UserEducation toEntity(UserEducationRequest userEducationRequest);
}
