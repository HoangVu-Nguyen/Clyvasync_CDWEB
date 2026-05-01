package com.relationservice.service;

public interface RelationService {
    void sendFriendRequest(String fromUserId, String toUserId);
    void acceptFriendRequest(String currentUserId, String fromUserId);
    void blockUser(String currentUserId, String targetUserId);
}
