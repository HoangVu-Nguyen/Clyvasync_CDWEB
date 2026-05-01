package com.relationservice.repository;

import com.relationservice.entity.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {
    @Query("MERGE (me:User {userId: $currentUserId}) " +
                  "MERGE (target:User {userId: $targetUserId}) " +
                  "WITH me, target " +
                  "OPTIONAL MATCH (me)-[r]-(target) " +
                  "DELETE r " +
                  "WITH me, target " +
                  "CREATE (me)-[b:FRIENDS_WITH {status: 'BLOCKED', createdAt: localdatetime(), updatedAt: localdatetime()}]->(target)")
    void blockUserInGraph(String currentUserId, String targetUserId);
    @Query("MATCH (req:User {userId: $fromUserId})-[rel:FRIENDS_WITH {status: 'PENDING'}]->(acc:User {userId: $currentUserId}) " +
            "SET rel.status = 'ACCEPTED', rel.updatedAt = localdatetime() " +
            "MERGE (acc)-[backRel:FRIENDS_WITH]->(req) " +
            "ON CREATE SET backRel.status = 'ACCEPTED', backRel.createdAt = localdatetime(), backRel.updatedAt = localdatetime() " +
            "ON MATCH SET backRel.status = 'ACCEPTED', backRel.updatedAt = localdatetime() " +
            "RETURN count(rel) > 0")
    boolean acceptFriendRequestInGraph(String currentUserId, String fromUserId);
}
