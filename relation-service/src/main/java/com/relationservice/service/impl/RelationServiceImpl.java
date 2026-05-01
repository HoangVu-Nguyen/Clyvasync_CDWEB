package com.relationservice.service.impl;

import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.dto.record.SpiceDbRel;
import com.commonlibrary.dto.schema.SpiceSchema;
import com.commonlibrary.service.social.SpiceDbService;
import com.relationservice.entity.node.UserNode;
import com.relationservice.entity.rel.UserRelation;
import com.relationservice.enums.RelationStatus;
import com.relationservice.repository.UserNodeRepository;
import com.relationservice.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RelationServiceImpl implements RelationService {

    private final UserNodeRepository userNodeRepository;
    private final SpiceDbService spiceDBService;

    @Override
    @Transactional
    public void sendFriendRequest(String fromUserId, String toUserId) {
        if (fromUserId.equals(toUserId)) throw new AppException(ResultCode.CANNOT_SEND_TO_SELF);

        UserNode fromNode = getOrCreateNode(fromUserId);
        UserNode toNode = getOrCreateNode(toUserId);

        // 1. Kiểm tra xem có đang bị Block không hoặc đã kết bạn/gửi request chưa
        boolean exists = fromNode.getRelations().stream()
                .anyMatch(r -> r.getTargetUser().getUserId().equals(toUserId));
        if (exists) throw new AppException(ResultCode.ALREADY_FRIENDS);

        // 2. Tạo Request
        UserRelation relation = UserRelation.builder()
                .status(RelationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .targetUser(toNode)
                .build();

        fromNode.getRelations().add(relation);
        userNodeRepository.save(fromNode);

        log.info(">>>> [RELATION] Friend request sent from {} to {}", fromUserId, toUserId);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(String currentUserId, String fromUserId) {
        // 1. GỌI DB ĐÚNG 1 LẦN: Chuyển PENDING -> ACCEPTED và tạo chiều ngược lại
        boolean isSuccess = userNodeRepository.acceptFriendRequestInGraph(currentUserId, fromUserId);

        if (!isSuccess) {
            throw new AppException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }

        List<SpiceDbRel> relsToWrite = List.of(
                // A -> B
                new SpiceDbRel(SpiceDBConstants.TargetType.USER.getValue(), currentUserId,
                        SpiceDBConstants.Relation.FRIEND.getValue(),
                        SpiceDBConstants.TargetType.USER.getValue(), fromUserId),
                // B -> A
                new SpiceDbRel(SpiceDBConstants.TargetType.USER.getValue(), fromUserId,
                        SpiceDBConstants.Relation.FRIEND.getValue(),
                        SpiceDBConstants.TargetType.USER.getValue(), currentUserId)
        );
        spiceDBService.writeRelationships(relsToWrite);

        log.info(">>>> [RELATION] Friend request accepted between {} and {} (Optimized 1-trip Cypher & Bulk SpiceDB)", currentUserId, fromUserId);
    }
    @Override
    @Transactional
    public void blockUser(String currentUserId, String targetUserId) {
        if (currentUserId.equals(targetUserId)) throw new AppException(ResultCode.CANNOT_BLOCK_SELF);

        userNodeRepository.blockUserInGraph(currentUserId, targetUserId);

        List<SpiceDbRel> relsToDelete = List.of(
                new SpiceDbRel(SpiceSchema.USER, currentUserId, SpiceSchema.FRIEND, SpiceSchema.USER, targetUserId),
                new SpiceDbRel(SpiceSchema.USER, targetUserId, SpiceSchema.FRIEND, SpiceSchema.USER, currentUserId)
        );
        spiceDBService.deleteRelationships(relsToDelete);

        spiceDBService.writeRelationship(
                SpiceDBConstants.TargetType.USER, currentUserId,
                SpiceDBConstants.Relation.BLOCKED,
                SpiceDBConstants.TargetType.USER, targetUserId
        );

        log.info(">>>> [RELATION] User {} blocked {}", currentUserId, targetUserId);
    }

    private UserNode getOrCreateNode(String userId) {
        return userNodeRepository.findById(userId).orElseGet(() -> {
            UserNode newNode = new UserNode();
            newNode.setUserId(userId);
            newNode.setRelations(new ArrayList<>());
            return userNodeRepository.save(newNode);
        });
    }
}