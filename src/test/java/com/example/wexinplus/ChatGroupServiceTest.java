package com.example.wexinplus;

import com.example.wexinplus.ChatGroup.ChatGroup;
import com.example.wexinplus.ChatGroup.ChatGroupService;
import com.example.wexinplus.GroupMember.GroupMember;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserService;
import com.example.wexinplus.dto.AddGroupMemberRequest;
import com.example.wexinplus.dto.CreateGroupRequest;
import com.example.wexinplus.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ChatGroupServiceTest {

    @Autowired
    private ChatGroupService chatGroupService;

    @Autowired
    private UserService userService;

    private User owner;
    private User member1;
    private User member2;

    @BeforeEach
    void setUp() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("groupowner");
        req1.setPassword("123456");
        req1.setNickname("群主");
        owner = userService.register(req1);

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("groupmember1");
        req2.setPassword("123456");
        req2.setNickname("成员1");
        member1 = userService.register(req2);

        RegisterRequest req3 = new RegisterRequest();
        req3.setUsername("groupmember2");
        req3.setPassword("123456");
        req3.setNickname("成员2");
        member2 = userService.register(req3);
    }

    @Test
    void testCreateGroup_Success() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setNotice("欢迎加入测试群");

        ChatGroup group = chatGroupService.createGroup(request);
        assertNotNull(group.getGroupId());
        assertEquals("测试群聊", group.getGroupName());
        assertEquals("欢迎加入测试群", group.getNotice());
        assertNotNull(group.getChatSession()); // 应该有对应的会话
    }

    @Test
    void testCreateGroup_WithMembers() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId(), member2.getUserId()));

        ChatGroup group = chatGroupService.createGroup(request);

        Map<String, Object> groupInfo = chatGroupService.getGroupInfo(group.getGroupId());
        List<Map<String, Object>> members = (List<Map<String, Object>>) groupInfo.get("members");
        assertEquals(3, members.size()); // 群主 + 2个成员
    }

    @Test
    void testAddMembers() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        ChatGroup group = chatGroupService.createGroup(request);

        AddGroupMemberRequest addRequest = new AddGroupMemberRequest();
        addRequest.setGroupId(group.getGroupId());
        addRequest.setOperatorId(owner.getUserId());
        addRequest.setMemberIds(List.of(member1.getUserId(), member2.getUserId()));

        List<GroupMember> newMembers = chatGroupService.addMembers(addRequest);
        assertEquals(2, newMembers.size());
    }

    @Test
    void testRemoveMember() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId()));
        ChatGroup group = chatGroupService.createGroup(request);

        chatGroupService.removeMember(group.getGroupId(), member1.getUserId(), owner.getUserId());

        Map<String, Object> groupInfo = chatGroupService.getGroupInfo(group.getGroupId());
        List<Map<String, Object>> members = (List<Map<String, Object>>) groupInfo.get("members");
        assertEquals(1, members.size()); // 只剩群主
    }

    @Test
    void testLeaveGroup() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId()));
        ChatGroup group = chatGroupService.createGroup(request);

        chatGroupService.leaveGroup(group.getGroupId(), member1.getUserId());

        Map<String, Object> groupInfo = chatGroupService.getGroupInfo(group.getGroupId());
        List<Map<String, Object>> members = (List<Map<String, Object>>) groupInfo.get("members");
        assertEquals(1, members.size()); // 只剩群主
    }

    @Test
    void testOwnerCannotLeave() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        ChatGroup group = chatGroupService.createGroup(request);

        assertThrows(RuntimeException.class, () -> {
            chatGroupService.leaveGroup(group.getGroupId(), owner.getUserId());
        });
    }

    @Test
    void testDisbandGroup() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId()));
        ChatGroup group = chatGroupService.createGroup(request);

        chatGroupService.disbandGroup(group.getGroupId(), owner.getUserId());

        assertThrows(RuntimeException.class, () -> {
            chatGroupService.getGroupInfo(group.getGroupId());
        });
    }

    @Test
    void testGetGroupInfo() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId()));
        ChatGroup group = chatGroupService.createGroup(request);

        Map<String, Object> info = chatGroupService.getGroupInfo(group.getGroupId());
        assertEquals("测试群聊", info.get("groupName"));
        assertEquals(owner.getUserId(), info.get("ownerId"));
        assertEquals(2, info.get("memberCount"));
    }

    @Test
    void testUpdateNotice() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        ChatGroup group = chatGroupService.createGroup(request);

        ChatGroup updated = chatGroupService.updateNotice(group.getGroupId(), "新公告", owner.getUserId());
        assertEquals("新公告", updated.getNotice());
    }

    @Test
    void testSearchGroups() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("Java学习群");
        chatGroupService.createGroup(request);

        List<ChatGroup> results = chatGroupService.searchGroups("Java");
        assertFalse(results.isEmpty());
    }

    @Test
    void testGetUserGroups() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(owner.getUserId());
        request.setGroupName("测试群聊");
        request.setMemberIds(List.of(member1.getUserId()));
        chatGroupService.createGroup(request);

        List<Map<String, Object>> ownerGroups = chatGroupService.getUserGroups(owner.getUserId());
        assertFalse(ownerGroups.isEmpty());

        List<Map<String, Object>> memberGroups = chatGroupService.getUserGroups(member1.getUserId());
        assertFalse(memberGroups.isEmpty());
    }
}
