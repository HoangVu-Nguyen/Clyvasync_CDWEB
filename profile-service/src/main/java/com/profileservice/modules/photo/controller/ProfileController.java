package com.profileservice.modules.photo.controller;

import com.commoncore.dto.response.ApiResponse;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;
import com.profileservice.modules.photo.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ApiResponse.success(profileService.getUserProfile(userId));
    }


}