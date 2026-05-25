package com.example.wexinplus.FriendGroup;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRelationshipRepository extends CrudRepository<FriendRelationship, Long> {

    // 查找用户的所有好友关系（主动添加的）
    List<FriendRelationship> findByUserUserId(Long userId);

    // 查找用户被添加的好友关系
    List<FriendRelationship> findByFriendUserId(Long userId);

    // 查找两个用户之间的好友关系
    Optional<FriendRelationship> findByUserUserIdAndFriendUserId(Long userId, Long friendId);

    // 查找某个分组下的所有好友关系
    List<FriendRelationship> findByFriendGroupGroupId(Long groupId);

    // 查找用户所有状态为已通过的好友关系
    List<FriendRelationship> findByUserUserIdAndStatus(Long userId, Integer status);

    // 查找用户收到的待验证好友请求
    List<FriendRelationship> findByFriendUserIdAndStatus(Long userId, Integer status);
}
