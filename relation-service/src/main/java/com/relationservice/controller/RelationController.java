package com.relationservice.controller;

import com.commoncore.dto.response.ApiResponse;import com.commonsecurity.secutiry.annotation.CurrentUserId;import com.relationservice.service.RelationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/relations")
@RequiredArgsConstructor
public class RelationController {

    private final RelationService relationService;

    @PostMapping("/friend-request/{toUserId}")
    public ApiResponse<Void> sendFriendRequest(
            @CurrentUserId String fromUserId,
            @PathVariable String toUserId) {

        relationService.sendFriendRequest(fromUserId, toUserId);
        return ApiResponse.success();
    }

    @PostMapping("/accept/{fromUserId}")
    public ApiResponse<Void> acceptFriendRequest(
            @CurrentUserId String currentUserId,
            @PathVariable String fromUserId) {

        relationService.acceptFriendRequest(currentUserId, fromUserId);
        return ApiResponse.success();
    }

    @PostMapping("/block/{targetUserId}")
    public ApiResponse<Void> blockUser(
            @CurrentUserId String currentUserId,
            @PathVariable String targetUserId) {

        relationService.blockUser(currentUserId, targetUserId);
        return ApiResponse.success();
    }
}