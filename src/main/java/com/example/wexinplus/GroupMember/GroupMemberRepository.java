package com.example.wexinplus.GroupMember;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends CrudRepository<GroupMember, Long> {

    // 查找用户加入的所有群聊
    List<GroupMember> findByUserUserId(Long userId);

    // 查找群聊的所有成员
    List<GroupMember> findByChatGroupGroupId(Long groupId);

    // 查找用户在某个群聊中的成员记录
    Optional<GroupMember> findByUserUserIdAndChatGroupGroupId(Long userId, Long groupId);

    // 检查用户是否已是群成员
    boolean existsByUserUserIdAndChatGroupGroupId(Long userId, Long groupId);
}
