package com.example.wexinplus.ChatGroup;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.ChatSession.ChatSessionRepository;
import com.example.wexinplus.GroupMember.GroupMember;
import com.example.wexinplus.GroupMember.GroupMemberRepository;
import com.example.wexinplus.SessionParticipant.SessionParticipant;
import com.example.wexinplus.SessionParticipant.SessionParticipantRepository;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserRepository;
import com.example.wexinplus.dto.AddGroupMemberRequest;
import com.example.wexinplus.dto.CreateGroupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatGroupService {

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private SessionParticipantRepository sessionParticipantRepository;

    /**
     * 创建群聊
     */
    @Transactional
    public ChatGroup createGroup(CreateGroupRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 创建群聊
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setGroupName(request.getGroupName());
        chatGroup.setNotice(request.getNotice());
        chatGroup.setOwner(owner);
        chatGroup = chatGroupRepository.save(chatGroup);

        // 创建对应的聊天会话
        ChatSession session = new ChatSession();
        session.setSessionType(2); // 群聊
        session = chatSessionRepository.save(session);

        // 关联群聊和会话
        chatGroup.setChatSession(session);
        chatGroup = chatGroupRepository.save(chatGroup);

        // 添加群主为成员（角色：群主）
        addMemberToGroup(chatGroup, owner, 1, session);

        // 添加初始成员
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(request.getOwnerId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("用户ID " + memberId + " 不存在"));
                    addMemberToGroup(chatGroup, member, 3, session); // 普通成员
                }
            }
        }

        return chatGroup;
    }

    /**
     * 添加群成员
     */
    @Transactional
    public List<GroupMember> addMembers(AddGroupMemberRequest request) {
        ChatGroup chatGroup = chatGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 验证操作人权限
        GroupMember operator = groupMemberRepository
                .findByUserUserIdAndChatGroupGroupId(request.getOperatorId(), request.getGroupId())
                .orElseThrow(() -> new RuntimeException("操作人不是群成员"));

        if (operator.getRole() != 1 && operator.getRole() != 2) {
            throw new RuntimeException("只有群主或管理员才能添加成员");
        }

        List<GroupMember> newMembers = new ArrayList<>();
        for (Long memberId : request.getMemberIds()) {
            if (groupMemberRepository.existsByUserUserIdAndChatGroupGroupId(memberId, request.getGroupId())) {
                continue; // 已是群成员，跳过
            }

            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("用户ID " + memberId + " 不存在"));

            GroupMember groupMember = addMemberToGroup(chatGroup, member, 3,
                    chatGroup.getChatSession());
            newMembers.add(groupMember);
        }

        return newMembers;
    }

    /**
     * 移除群成员
     */
    @Transactional
    public void removeMember(Long groupId, Long memberId, Long operatorId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 验证操作人权限
        GroupMember operator = groupMemberRepository
                .findByUserUserIdAndChatGroupGroupId(operatorId, groupId)
                .orElseThrow(() -> new RuntimeException("操作人不是群成员"));

        if (operator.getRole() != 1 && operator.getRole() != 2) {
            throw new RuntimeException("只有群主或管理员才能移除成员");
        }

        // 不能移除群主
        GroupMember target = groupMemberRepository
                .findByUserUserIdAndChatGroupGroupId(memberId, groupId)
                .orElseThrow(() -> new RuntimeException("该用户不是群成员"));

        if (target.getRole() == 1) {
            throw new RuntimeException("不能移除群主");
        }

        // 移除会话参与者
        sessionParticipantRepository
                .findByUserUserIdAndChatSessionSessionId(memberId, chatGroup.getChatSession().getSessionId())
                .ifPresent(sessionParticipantRepository::delete);

        groupMemberRepository.delete(target);
    }

    /**
     * 退出群聊
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository
                .findByUserUserIdAndChatGroupGroupId(userId, groupId)
                .orElseThrow(() -> new RuntimeException("你不是该群成员"));

        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 群主不能退出，只能转让或解散
        if (member.getRole() == 1) {
            throw new RuntimeException("群主不能退出群聊，请先转让群主身份或解散群聊");
        }

        // 移除会话参与者
        sessionParticipantRepository
                .findByUserUserIdAndChatSessionSessionId(userId, chatGroup.getChatSession().getSessionId())
                .ifPresent(sessionParticipantRepository::delete);

        groupMemberRepository.delete(member);
    }

    /**
     * 解散群聊
     */
    @Transactional
    public void disbandGroup(Long groupId, Long ownerId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        if (!chatGroup.getOwner().getUserId().equals(ownerId)) {
            throw new RuntimeException("只有群主才能解散群聊");
        }

        // 删除所有群成员
        List<GroupMember> members = groupMemberRepository.findByChatGroupGroupId(groupId);
        groupMemberRepository.deleteAll(members);

        // 删除会话参与者
        List<SessionParticipant> participants = sessionParticipantRepository
                .findByChatSessionSessionId(chatGroup.getChatSession().getSessionId());
        sessionParticipantRepository.deleteAll(participants);

        // 删除会话
        chatSessionRepository.delete(chatGroup.getChatSession());

        // 删除群聊
        chatGroupRepository.delete(chatGroup);
    }

    /**
     * 获取群聊信息（含成员列表）
     */
    public Map<String, Object> getGroupInfo(Long groupId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", chatGroup.getGroupId());
        result.put("groupName", chatGroup.getGroupName());
        result.put("notice", chatGroup.getNotice());
        result.put("createTime", chatGroup.getCreateTime());
        result.put("ownerId", chatGroup.getOwner().getUserId());
        result.put("ownerName", chatGroup.getOwner().getUsername());
        result.put("sessionId", chatGroup.getChatSession().getSessionId());

        // 获取成员列表
        List<GroupMember> members = groupMemberRepository.findByChatGroupGroupId(groupId);
        List<Map<String, Object>> memberList = members.stream().map(m -> {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("memberId", m.getMemberId());
            memberMap.put("userId", m.getUser().getUserId());
            memberMap.put("username", m.getUser().getUsername());
            memberMap.put("nickname", m.getUser().getNickname());
            memberMap.put("avatar", m.getUser().getAvatar());
            memberMap.put("groupNickname", m.getGroupNickname());
            memberMap.put("role", m.getRole());
            memberMap.put("joinTime", m.getJoinTime());
            return memberMap;
        }).collect(Collectors.toList());

        result.put("members", memberList);
        result.put("memberCount", memberList.size());

        return result;
    }

    /**
     * 搜索群聊
     */
    public List<ChatGroup> searchGroups(String keyword) {
        return chatGroupRepository.findByGroupNameContaining(keyword);
    }

    /**
     * 获取用户加入的所有群聊
     */
    public List<Map<String, Object>> getUserGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserUserId(userId);

        return memberships.stream().map(m -> {
            ChatGroup group = m.getChatGroup();
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupId", group.getGroupId());
            groupMap.put("groupName", group.getGroupName());
            groupMap.put("notice", group.getNotice());
            groupMap.put("role", m.getRole());
            groupMap.put("groupNickname", m.getGroupNickname());
            groupMap.put("sessionId", group.getChatSession().getSessionId());
            groupMap.put("ownerId", group.getOwner().getUserId());
            groupMap.put("memberCount",
                    groupMemberRepository.findByChatGroupGroupId(group.getGroupId()).size());
            return groupMap;
        }).collect(Collectors.toList());
    }

    /**
     * 更新群公告
     */
    @Transactional
    public ChatGroup updateNotice(Long groupId, String notice, Long operatorId) {
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        GroupMember operator = groupMemberRepository
                .findByUserUserIdAndChatGroupGroupId(operatorId, groupId)
                .orElseThrow(() -> new RuntimeException("操作人不是群成员"));

        if (operator.getRole() != 1 && operator.getRole() != 2) {
            throw new RuntimeException("只有群主或管理员才能修改群公告");
        }

        chatGroup.setNotice(notice);
        return chatGroupRepository.save(chatGroup);
    }

    // ==================== 私有方法 ====================

    private GroupMember addMemberToGroup(ChatGroup chatGroup, User user, int role, ChatSession session) {
        // 添加群成员
        GroupMember groupMember = new GroupMember();
        groupMember.setChatGroup(chatGroup);
        groupMember.setUser(user);
        groupMember.setRole(role);
        groupMember = groupMemberRepository.save(groupMember);

        // 添加会话参与者
        SessionParticipant participant = new SessionParticipant();
        participant.setChatSession(session);
        participant.setUser(user);
        sessionParticipantRepository.save(participant);

        return groupMember;
    }
}
