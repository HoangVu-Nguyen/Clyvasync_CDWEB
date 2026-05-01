package com.relationservice.entity.rel;

import com.relationservice.entity.node.UserNode;
import com.relationservice.enums.RelationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import java.time.LocalDateTime;

@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelation {

    @RelationshipId
    private Long id;

    private RelationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TargetNode
    private UserNode targetUser;
}