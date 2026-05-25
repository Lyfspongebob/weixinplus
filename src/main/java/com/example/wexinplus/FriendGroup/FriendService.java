package com.example.wexinplus.FriendGroup;

import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserRepository;
import com.example.wexinplus.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendGroupRepository friendGroupRepository;

    @Autowired
    private FriendRelationshipRepository friendRelationshipRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== 好友分组管理 ====================

    /**
     * 创建好友分组
     */
    @Transactional
    public FriendGroup createGroup(FriendGroupRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已存在同名分组
        FriendGroup existing = friendGroupRepository.findByUserUserIdAndGroupName(
                request.getUserId(), request.getGroupName());
        if (existing != null) {
            throw new RuntimeException("分组名称已存在");
        }

        FriendGroup group = new FriendGroup();
        group.setUser(user);
        group.setGroupName(request.getGroupName());
        return friendGroupRepository.save(group);
    }

    /**
     * 获取用户的所有好友分组（含每个分组下的好友列表）
     */
    public List<Map<String, Object>> getGroupsWithFriends(Long userId) {
        List<FriendGroup> groups = friendGroupRepository.findByUserUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FriendGroup group : groups) {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupId", group.getGroupId());
            groupMap.put("groupName", group.getGroupName());

            // 获取该分组下所有已通过的好友
            List<FriendRelationship> relationships = friendRelationshipRepository
                    .findByFriendGroupGroupId(group.getGroupId());

            List<Map<String, Object>> friends = relationships.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus() == 1) // 只返回已通过的好友
                    .map(r -> {
                        Map<String, Object> friendMap = new HashMap<>();
                        friendMap.put("relationshipId", r.getRelationshipId());
                        friendMap.put("userId", r.getFriend().getUserId());
                        friendMap.put("username", r.getFriend().getUsername());
                        friendMap.put("nickname", r.getFriend().getNickname());
                        friendMap.put("avatar", r.getFriend().getAvatar());
                        friendMap.put("status", r.getFriend().getStatus());
                        friendMap.put("remark", r.getRemark());
                        return friendMap;
                    })
                    .collect(Collectors.toList());

            groupMap.put("friends", friends);
            result.add(groupMap);
        }

        return result;
    }

    /**
     * 删除好友分组
     */
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        FriendGroup group = friendGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在"));

        if (!group.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该分组");
        }

        // 获取默认分组
        FriendGroup defaultGroup = friendGroupRepository
                .findByUserUserIdAndGroupName(userId, "我的好友");
        if (defaultGroup == null) {
            defaultGroup = createDefaultGroup(userId);
        }

        // 将该分组下的好友移动到默认分组
        List<FriendRelationship> relationships = friendRelationshipRepository
                .findByFriendGroupGroupId(groupId);
        for (FriendRelationship rel : relationships) {
            rel.setFriendGroup(defaultGroup);
            friendRelationshipRepository.save(rel);
        }

        friendGroupRepository.delete(group);
    }

    /**
     * 重命名好友分组
     */
    @Transactional
    public FriendGroup renameGroup(Long groupId, String newName, Long userId) {
        FriendGroup group = friendGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在"));

        if (!group.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该分组");
        }

        // 检查新名称是否已存在
        FriendGroup existing = friendGroupRepository
                .findByUserUserIdAndGroupName(userId, newName);
        if (existing != null && !existing.getGroupId().equals(groupId)) {
            throw new RuntimeException("分组名称已存在");
        }

        group.setGroupName(newName);
        return friendGroupRepository.save(group);
    }

    // ==================== 好友关系管理 ====================

    /**
     * 发送好友申请
     */
    @Transactional
    public FriendRelationship applyFriend(FriendApplyRequest request) {
        // 检查是否已存在好友关系
        var existingRel = friendRelationshipRepository
                .findByUserUserIdAndFriendUserId(request.getUserId(), request.getFriendId());
        if (existingRel.isPresent()) {
            FriendRelationship rel = existingRel.get();
            if (rel.getStatus() == 1) {
                throw new RuntimeException("你们已经是好友了");
            } else if (rel.getStatus() == 0) {
                throw new RuntimeException("已发送过好友申请，请等待对方处理");
            } else if (rel.getStatus() == 3) {
                // 如果之前被删除，重新发送申请
                rel.setStatus(0);
                rel.setApplyMessage(request.getApplyMessage());
                rel.setUpdateTime(LocalDateTime.now());
                return friendRelationshipRepository.save(rel);
            }
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User friend = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> new RuntimeException("好友用户不存在"));

        // 获取分组
        FriendGroup group;
        if (request.getGroupId() != null) {
            group = friendGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RuntimeException("分组不存在"));
        } else {
            group = friendGroupRepository.findByUserUserIdAndGroupName(request.getUserId(), "我的好友");
            if (group == null) {
                group = createDefaultGroup(request.getUserId());
            }
        }

        FriendRelationship relationship = new FriendRelationship();
        relationship.setUser(user);
        relationship.setFriend(friend);
        relationship.setFriendGroup(group);
        relationship.setRemark(request.getRemark());
        relationship.setStatus(0); // 待验证
        relationship.setApplyMessage(request.getApplyMessage());

        return friendRelationshipRepository.save(relationship);
    }

    /**
     * 处理好友申请（通过/拒绝）
     */
    @Transactional
    public FriendRelationship handleFriendApply(FriendHandleRequest request) {
        FriendRelationship relationship = friendRelationshipRepository
                .findById(request.getRelationshipId())
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));

        if (request.getAction() == 1) {
            // 通过申请
            relationship.setStatus(1);
            relationship.setUpdateTime(LocalDateTime.now());
            if (request.getRemark() != null) {
                relationship.setRemark(request.getRemark());
            }

            // 同时建立反向好友关系（双向好友）
            // 检查反向关系是否已存在
            var reverseRel = friendRelationshipRepository
                    .findByUserUserIdAndFriendUserId(
                            relationship.getFriend().getUserId(),
                            relationship.getUser().getUserId());

            if (reverseRel.isEmpty()) {
                // 获取对方用户的默认分组
                FriendGroup friendDefaultGroup = friendGroupRepository
                        .findByUserUserIdAndGroupName(
                                relationship.getFriend().getUserId(), "我的好友");
                if (friendDefaultGroup == null) {
                    friendDefaultGroup = createDefaultGroup(relationship.getFriend().getUserId());
                }

                FriendRelationship reverseRelationship = new FriendRelationship();
                reverseRelationship.setUser(relationship.getFriend());
                reverseRelationship.setFriend(relationship.getUser());
                reverseRelationship.setFriendGroup(friendDefaultGroup);
                reverseRelationship.setStatus(1); // 直接通过
                reverseRelationship.setApplyMessage("我们已经是好友了，开始聊天吧！");
                friendRelationshipRepository.save(reverseRelationship);
            } else {
                FriendRelationship rev = reverseRel.get();
                rev.setStatus(1);
                rev.setUpdateTime(LocalDateTime.now());
                friendRelationshipRepository.save(rev);
            }

        } else if (request.getAction() == 2) {
            // 拒绝申请
            relationship.setStatus(2);
            relationship.setUpdateTime(LocalDateTime.now());
        } else {
            throw new RuntimeException("无效的操作");
        }

        return friendRelationshipRepository.save(relationship);
    }

    /**
     * 删除好友
     */
    @Transactional
    public void deleteFriend(Long relationshipId, Long userId) {
        FriendRelationship relationship = friendRelationshipRepository
                .findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        if (!relationship.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        // 标记为已删除
        relationship.setStatus(3);
        relationship.setUpdateTime(LocalDateTime.now());
        friendRelationshipRepository.save(relationship);

        // 同时删除或标记反向关系
        var reverseRel = friendRelationshipRepository
                .findByUserUserIdAndFriendUserId(
                        relationship.getFriend().getUserId(),
                        relationship.getUser().getUserId());

        reverseRel.ifPresent(rev -> {
            rev.setStatus(3);
            rev.setUpdateTime(LocalDateTime.now());
            friendRelationshipRepository.save(rev);
        });
    }

    /**
     * 移动好友到其他分组
     */
    @Transactional
    public FriendRelationship moveFriend(FriendMoveRequest request) {
        FriendRelationship relationship = friendRelationshipRepository
                .findById(request.getRelationshipId())
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        FriendGroup newGroup = friendGroupRepository.findById(request.getNewGroupId())
                .orElseThrow(() -> new RuntimeException("目标分组不存在"));

        relationship.setFriendGroup(newGroup);
        relationship.setUpdateTime(LocalDateTime.now());
        return friendRelationshipRepository.save(relationship);
    }

    /**
     * 重新发送验证信息
     */
    @Transactional
    public FriendRelationship resendApply(Long relationshipId, String applyMessage) {
        FriendRelationship relationship = friendRelationshipRepository
                .findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));

        if (relationship.getStatus() != 3) {
            throw new RuntimeException("只有已删除的好友关系才能重新发送验证");
        }

        relationship.setStatus(0); // 重新设置为待验证
        relationship.setApplyMessage(applyMessage);
        relationship.setUpdateTime(LocalDateTime.now());
        return friendRelationshipRepository.save(relationship);
    }

    /**
     * 获取用户收到的待处理好友申请
     */
    public List<FriendRelationship> getPendingRequests(Long userId) {
        return friendRelationshipRepository.findByFriendUserIdAndStatus(userId, 0);
    }

    /**
     * 获取用户的所有好友（已通过的）
     */
    public List<Map<String, Object>> getAllFriends(Long userId) {
        List<FriendRelationship> relationships = friendRelationshipRepository
                .findByUserUserIdAndStatus(userId, 1);

        return relationships.stream().map(r -> {
            Map<String, Object> friendMap = new HashMap<>();
            friendMap.put("relationshipId", r.getRelationshipId());
            friendMap.put("userId", r.getFriend().getUserId());
            friendMap.put("username", r.getFriend().getUsername());
            friendMap.put("nickname", r.getFriend().getNickname());
            friendMap.put("avatar", r.getFriend().getAvatar());
            friendMap.put("status", r.getFriend().getStatus());
            friendMap.put("remark", r.getRemark());
            friendMap.put("groupId", r.getFriendGroup().getGroupId());
            friendMap.put("groupName", r.getFriendGroup().getGroupName());
            return friendMap;
        }).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private FriendGroup createDefaultGroup(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FriendGroup defaultGroup = new FriendGroup();
        defaultGroup.setUser(user);
        defaultGroup.setGroupName("我的好友");
        return friendGroupRepository.save(defaultGroup);
    }
}
