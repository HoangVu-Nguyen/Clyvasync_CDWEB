package com.commonlibrary.service.social;

import com.authzed.api.v1.*;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.dto.record.SpiceDbRel;
import com.commonlibrary.dto.schema.SpiceSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpiceDbService {

    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsStub;

    // 1. Check: Resource trước, Subject sau
    // 1. Hàm kiểm tra Quyền (Permission) - Dùng cho VIEW, EDIT, DELETE...
    public boolean checkPermission(SpiceDBConstants.TargetType resourceType, String resourceId,
                                   SpiceDBConstants.Permission permission,
                                   SpiceDBConstants.TargetType subjectType, String subjectId) {
        return executeCheck(resourceType, resourceId, permission.getValue(), subjectType, subjectId);
    }

    // 2. Hàm kiểm tra Quan hệ (Relation) - Dùng cho FRIEND, OWNER, MANAGER...
    public boolean checkRelation(SpiceDBConstants.TargetType resourceType, String resourceId,
                                 SpiceDBConstants.Relation relation,
                                 SpiceDBConstants.TargetType subjectType, String subjectId) {
        return executeCheck(resourceType, resourceId, relation.getValue(), subjectType, subjectId);
    }

    // 3. Hàm Private thực thi gọi gRPC (Tái sử dụng code)
    private boolean executeCheck(SpiceDBConstants.TargetType resourceType, String resourceId,
                                 String actionToCheck, // Nhận String từ .getValue() của cả 2 Enum
                                 SpiceDBConstants.TargetType subjectType, String subjectId) {
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setResource(ObjectReference.newBuilder()
                        .setObjectType(resourceType.getValue())
                        .setObjectId(resourceId))
                .setPermission(actionToCheck) // SpiceDB API dùng chung trường này cho cả Relation và Permission
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectType(subjectType.getValue())
                                .setObjectId(subjectId)))
                .build();

        return permissionsStub.checkPermission(request).getPermissionship()
                == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
    }

    // 2. Write: Resource trước, Subject sau
    public void writeRelationship(SpiceDBConstants.TargetType resourceType, String resourceId,
                                  SpiceDBConstants.Relation relation,
                                  SpiceDBConstants.TargetType subjectType, String subjectId) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH) // Dùng TOUCH thay vì CREATE để tránh lỗi nếu đã tồn tại
                        .setRelationship(Relationship.newBuilder()
                                .setResource(ObjectReference.newBuilder()
                                        .setObjectType(resourceType.getValue())
                                        .setObjectId(resourceId))
                                .setRelation(relation.getValue())
                                .setSubject(SubjectReference.newBuilder()
                                        .setObject(ObjectReference.newBuilder()
                                                .setObjectType(subjectType.getValue())
                                                .setObjectId(subjectId)))))
                .build();

        permissionsStub.writeRelationships(request);
    }

    // 3. Delete: Giữ nguyên thứ tự Resource trước, Subject sau
    public void deleteRelationship(String resourceType, String resourceId, String relation,
                                   String subjectType, String subjectId) {
        try {
            WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                    .addUpdates(RelationshipUpdate.newBuilder()
                            .setOperation(RelationshipUpdate.Operation.OPERATION_DELETE)
                            .setRelationship(Relationship.newBuilder()
                                    .setResource(ObjectReference.newBuilder()
                                            .setObjectType(resourceType)
                                            .setObjectId(resourceId))
                                    .setRelation(relation)
                                    .setSubject(SubjectReference.newBuilder()
                                            .setObject(ObjectReference.newBuilder()
                                                    .setObjectType(subjectType)
                                                    .setObjectId(subjectId)))))
                    .build();
            permissionsStub.writeRelationships(request);
            log.info("🗑️ SpiceDB Deleted: {}:{}#{}@{}[:{}]", resourceType, resourceId, relation, subjectType, subjectId);
        } catch (Exception e) {
            log.error("SpiceDB Delete Error: {}", e.getMessage());
        }
    }
    public void deleteRelationships(List<SpiceDbRel> relationships) {
        if (relationships == null || relationships.isEmpty()) return;
        try {
            WriteRelationshipsRequest.Builder builder = WriteRelationshipsRequest.newBuilder();
            for (SpiceDbRel rel : relationships) {
                String subType = rel.subjectType() != null ? rel.subjectType() : SpiceSchema.USER;
                builder.addUpdates(buildUpdate(RelationshipUpdate.Operation.OPERATION_DELETE, rel.resourceType(), rel.resourceId(), rel.relation(), subType, rel.subjectId()));
            }
            permissionsStub.writeRelationships(builder.build());
        } catch (Exception e) {
            log.error("Bulk Delete Error: {}", e.getMessage());
        }
    }
    private RelationshipUpdate buildUpdate(RelationshipUpdate.Operation op, String resType, String resId, String rel, String subType, String subId) {
        return RelationshipUpdate.newBuilder()
                .setOperation(op)
                .setRelationship(Relationship.newBuilder()
                        .setResource(buildObjRef(resType, resId))
                        .setRelation(rel)
                        .setSubject(SubjectReference.newBuilder().setObject(buildObjRef(subType, subId)).build())
                        .build())
                .build();
    }
    private ObjectReference buildObjRef(String type, String id) {
        return ObjectReference.newBuilder().setObjectType(type).setObjectId(id).build();
    }
    // Hàm xóa quan hệ với Subject Relation (Ví dụ: xóa viewer là user:123#friend)
    public void deleteRelationshipWithSubjectRelation(
            String resourceType, String resourceId, String relation,
            String subjectType, String subjectId, String subjectRelation) {
        try {
            WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                    .addUpdates(RelationshipUpdate.newBuilder()
                            .setOperation(RelationshipUpdate.Operation.OPERATION_DELETE) // <--- Chế độ XÓA
                            .setRelationship(Relationship.newBuilder()
                                    .setResource(ObjectReference.newBuilder()
                                            .setObjectType(resourceType)
                                            .setObjectId(resourceId)
                                            .build())
                                    .setRelation(relation)
                                    .setSubject(SubjectReference.newBuilder()
                                            .setObject(ObjectReference.newBuilder()
                                                    .setObjectType(subjectType)
                                                    .setObjectId(subjectId)
                                                    .build())
                                            .setOptionalRelation(subjectRelation) // <--- Phải có cái này để xóa đúng #friend
                                            .build())
                                    .build())
                            .build())
                    .build();
            permissionsStub.writeRelationships(request);
            log.info("🗑️ SpiceDB: Deleted Relationship with Subject Relation: {}#{} -> {}:{}#{}",
                    resourceType, resourceId, relation, subjectType, subjectId, subjectRelation);
        } catch (Exception e) {
            log.error("SpiceDB Delete SubjectRel Error: {}", e.getMessage());
            // Lưu ý: Thường xóa không tìm thấy SpiceDB sẽ không báo lỗi gRPC,
            // nhưng nếu có lỗi kết nối thì sẽ log ở đây.
        }
    }
    // Hàm hỗ trợ Subject Relation (Ví dụ: viewer là user:123#friend)
    public void writeRelationshipWithSubjectRelation(
            String resourceType, String resourceId, String relation,
            String subjectType, String subjectId, String subjectRelation) {
        try {
            WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                    .addUpdates(RelationshipUpdate.newBuilder()
                            .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
                            .setRelationship(Relationship.newBuilder()
                                    .setResource(ObjectReference.newBuilder()
                                            .setObjectType(resourceType)
                                            .setObjectId(resourceId)
                                            .build())
                                    .setRelation(relation)
                                    .setSubject(SubjectReference.newBuilder()
                                            .setObject(ObjectReference.newBuilder()
                                                    .setObjectType(subjectType)
                                                    .setObjectId(subjectId)
                                                    .build())
                                            .setOptionalRelation(subjectRelation) // <--- QUAN TRỌNG: #friend
                                            .build())
                                    .build())
                            .build())
                    .build();
            permissionsStub.writeRelationships(request);
        } catch (Exception e) {
            log.error("SpiceDB SubjectRel Error: {}", e.getMessage());
            throw new RuntimeException("Failed to update permission");
        }
    }
    public void writeRelationships(List<SpiceDbRel> relationships) {
        if (relationships == null || relationships.isEmpty()) return;
        try {
            WriteRelationshipsRequest.Builder builder = WriteRelationshipsRequest.newBuilder();
            for (SpiceDbRel rel : relationships) {
                String subType = rel.subjectType() != null ? rel.subjectType() : SpiceDBConstants.TargetType.USER.getValue();
                // Dùng OPERATION_TOUCH để Ghi đè/Thêm mới an toàn
                builder.addUpdates(buildUpdate(RelationshipUpdate.Operation.OPERATION_TOUCH,
                        rel.resourceType(), rel.resourceId(),
                        rel.relation(), subType, rel.subjectId()));
            }
            permissionsStub.writeRelationships(builder.build());
        } catch (Exception e) {
            log.error("Bulk Write Error: {}", e.getMessage());
        }
    }
}