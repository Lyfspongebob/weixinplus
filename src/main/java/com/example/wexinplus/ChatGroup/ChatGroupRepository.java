package com.example.wexinplus.ChatGroup;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChatGroupRepository extends CrudRepository<ChatGroup, Long> {

    // 根据群主ID查找群聊
    List<ChatGroup> findByOwnerUserId(Long ownerId);

    // 根据群名称模糊搜索
    List<ChatGroup> findByGroupNameContaining(String groupName);
}
