package com.example.wexinplus.FriendGroup;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendGroupRepository extends CrudRepository<FriendGroup, Long> {

    // 根据用户ID查找该用户的所有好友分组
    List<FriendGroup> findByUserUserId(Long userId);

    // 根据用户ID和分组名称查找分组
    FriendGroup findByUserUserIdAndGroupName(Long userId, String groupName);
}
