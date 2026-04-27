package com.profileservice.modules.profile.service;


import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface IEducationService {
    List<UserEducationResponse> getEducationsByUserId(String userId);
}
