package com.relationservice.entity.node;

import com.relationservice.entity.rel.UserRelation;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("User")
@Data
public class UserNode {
    @Id
    private String userId;

    private String username;
    private String avatarUrl;

    @Relationship(type = "FRIENDS_WITH", direction = Relationship.Direction.OUTGOING)
    private List<UserRelation> relations;
}