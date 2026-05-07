package com.profileservice.modules.profile.controller;

import com.commoncore.dto.response.ApiResponse;
import com.commonsecurity.secutiry.annotation.CurrentUserId;

import com.profileservice.modules.profile.dto.request.UpdateProfileRequest;
import com.profileservice.modules.profile.dto.response.UserHeaderResponse;
import com.profileservice.modules.profile.dto.response.UserProfileResponse;
import com.profileservice.modules.profile.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;


    /**
     * Lấy profile chi tiết của một người dùng bất kỳ.
     * viewerId được lấy tự động từ JWT để check quyền (SpiceDB).
     */
    @GetMapping("/{ownerId}")
    public ApiResponse<UserProfileResponse> getProfileDetail(
            @PathVariable String ownerId,
            @CurrentUserId String viewerId) {
        return ApiResponse.success(profileService.getProfileDetail(ownerId, viewerId));
    }

    /**
     * Lấy thông tin Header của chính người dùng đang đăng nhập.
     * Không cần truyền ID lên URL vì đã có @CurrentUserId lo.
     */
    @GetMapping("/me/header")
    public ApiResponse<UserHeaderResponse> getMyHeaderInfo(@CurrentUserId String userId) {
        return ApiResponse.success(profileService.getHeaderInfo(userId));
    }

    /**
     * Lấy profile của chính mình (thường dùng cho trang cá nhân).
     */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@CurrentUserId String userId) {
        // Chính mình xem mình nên ownerId và viewerId là một
        return ApiResponse.success(profileService.getProfileDetail(userId, userId));
    }
    @PutMapping("")
    public ApiResponse<Void> updateProfile(@CurrentUserId String userId, @RequestBody UpdateProfileRequest request) {
        System.out.println(request);
       profileService.updateProfile(userId,request);
        return ApiResponse.success();
    }
}