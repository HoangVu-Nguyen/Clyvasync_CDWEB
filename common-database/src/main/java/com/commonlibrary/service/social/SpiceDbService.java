package com.commonlibrary.service.social;

import com.authzed.api.v1.*;
import com.commonlibrary.constant.SpiceDBConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpiceDbService {

    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsStub;

    public boolean checkPermission(SpiceDBConstants.TargetType subjectType, String subjectId,
                                   SpiceDBConstants.Permission permission,
                                   SpiceDBConstants.TargetType resourceType, String resourceId) {

        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setResource(ObjectReference.newBuilder()
                        .setObjectType(resourceType.getValue())
                        .setObjectId(String.valueOf(resourceId)))
                .setPermission(permission.getValue())
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectType(subjectType.getValue())
                                .setObjectId(String.valueOf(subjectId))))
                .build();

        return permissionsStub.checkPermission(request).getPermissionship()
                == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
    }


    public void writeRelationship(SpiceDBConstants.TargetType subjectType, Long subjectId,
                                  SpiceDBConstants.Relation relation,
                                  SpiceDBConstants.TargetType resourceType, Long resourceId) {

        Relationship relationship = Relationship.newBuilder()
                .setResource(ObjectReference.newBuilder()
                        .setObjectType(resourceType.getValue())
                        .setObjectId(String.valueOf(resourceId)))
                .setRelation(relation.getValue())
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectType(subjectType.getValue())
                                .setObjectId(String.valueOf(subjectId))))
                .build();

        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                        .setRelationship(relationship))
                .build();

        permissionsStub.writeRelationships(request);
    }
}