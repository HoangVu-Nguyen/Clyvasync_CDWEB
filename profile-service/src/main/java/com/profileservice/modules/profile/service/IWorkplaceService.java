package com.profileservice.modules.profile.service;

import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface IWorkplaceService {
    List<UserWorkplaceResponse> getWorkplacesByUserId(String userId);
}
