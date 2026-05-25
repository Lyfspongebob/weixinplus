package com.example.wexinplus;

import com.example.wexinplus.FriendGroup.*;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserService;
import com.example.wexinplus.dto.*;
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
public class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendGroupRepository friendGroupRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("frienduser1");
        req1.setPassword("123456");
        req1.setNickname("好友用户1");
        user1 = userService.register(req1);

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("frienduser2");
        req2.setPassword("123456");
        req2.setNickname("好友用户2");
        user2 = userService.register(req2);
    }

    @Test
    void testCreateGroup_Success() {
        FriendGroupRequest request = new FriendGroupRequest();
        request.setUserId(user1.getUserId());
        request.setGroupName("家人");

        FriendGroup group = friendService.createGroup(request);
        assertNotNull(group.getGroupId());
        assertEquals("家人", group.getGroupName());
    }

    @Test
    void testCreateGroup_DuplicateName() {
        FriendGroupRequest request = new FriendGroupRequest();
        request.setUserId(user1.getUserId());
        request.setGroupName("家人");
        friendService.createGroup(request);

        assertThrows(RuntimeException.class, () -> {
            friendService.createGroup(request);
        });
    }

    @Test
    void testGetGroupsWithFriends() {
        List<Map<String, Object>> groups = friendService.getGroupsWithFriends(user1.getUserId());
        assertFalse(groups.isEmpty());
        // 默认有"我的好友"分组
        assertTrue(groups.stream().anyMatch(g -> "我的好友".equals(g.get("groupName"))));
    }

    @Test
    void testApplyAndHandleFriend() {
        // user1 向 user2 发送好友申请
        FriendApplyRequest applyRequest = new FriendApplyRequest();
        applyRequest.setUserId(user1.getUserId());
        applyRequest.setFriendId(user2.getUserId());
        applyRequest.setApplyMessage("你好，加个好友吧");

        FriendRelationship relationship = friendService.applyFriend(applyRequest);
        assertNotNull(relationship.getRelationshipId());
        assertEquals(0, relationship.getStatus()); // 待验证

        // user2 获取待处理申请
        List<FriendRelationship> pending = friendService.getPendingRequests(user2.getUserId());
        assertFalse(pending.isEmpty());

        // user2 通过申请
        FriendHandleRequest handleRequest = new FriendHandleRequest();
        handleRequest.setRelationshipId(relationship.getRelationshipId());
        handleRequest.setAction(1); // 通过
        handleRequest.setRemark("好友备注");

        FriendRelationship handled = friendService.handleFriendApply(handleRequest);
        assertEquals(1, handled.getStatus()); // 已通过

        // 验证双向好友关系
        List<Map<String, Object>> user1Friends = friendService.getAllFriends(user1.getUserId());
        assertTrue(user1Friends.stream().anyMatch(f -> f.get("userId").equals(user2.getUserId())));

        List<Map<String, Object>> user2Friends = friendService.getAllFriends(user2.getUserId());
        assertTrue(user2Friends.stream().anyMatch(f -> f.get("userId").equals(user1.getUserId())));
    }

    @Test
    void testRejectFriendApply() {
        FriendApplyRequest applyRequest = new FriendApplyRequest();
        applyRequest.setUserId(user1.getUserId());
        applyRequest.setFriendId(user2.getUserId());
        FriendRelationship relationship = friendService.applyFriend(applyRequest);

        FriendHandleRequest handleRequest = new FriendHandleRequest();
        handleRequest.setRelationshipId(relationship.getRelationshipId());
        handleRequest.setAction(2); // 拒绝

        FriendRelationship handled = friendService.handleFriendApply(handleRequest);
        assertEquals(2, handled.getStatus()); // 已拒绝
    }

    @Test
    void testDeleteFriend() {
        // 先添加好友
        FriendApplyRequest applyRequest = new FriendApplyRequest();
        applyRequest.setUserId(user1.getUserId());
        applyRequest.setFriendId(user2.getUserId());
        FriendRelationship relationship = friendService.applyFriend(applyRequest);

        FriendHandleRequest handleRequest = new FriendHandleRequest();
        handleRequest.setRelationshipId(relationship.getRelationshipId());
        handleRequest.setAction(1);
        friendService.handleFriendApply(handleRequest);

        // 删除好友
        friendService.deleteFriend(relationship.getRelationshipId(), user1.getUserId());

        // 验证已删除
        List<Map<String, Object>> user1Friends = friendService.getAllFriends(user1.getUserId());
        assertTrue(user1Friends.stream().noneMatch(f -> f.get("userId").equals(user2.getUserId())));
    }

    @Test
    void testMoveFriend() {
        // 先添加好友
        FriendApplyRequest applyRequest = new FriendApplyRequest();
        applyRequest.setUserId(user1.getUserId());
        applyRequest.setFriendId(user2.getUserId());
        FriendRelationship relationship = friendService.applyFriend(applyRequest);

        FriendHandleRequest handleRequest = new FriendHandleRequest();
        handleRequest.setRelationshipId(relationship.getRelationshipId());
        handleRequest.setAction(1);
        friendService.handleFriendApply(handleRequest);

        // 创建新分组
        FriendGroupRequest groupRequest = new FriendGroupRequest();
        groupRequest.setUserId(user1.getUserId());
        groupRequest.setGroupName("同事");
        FriendGroup newGroup = friendService.createGroup(groupRequest);

        // 移动好友到新分组
        FriendMoveRequest moveRequest = new FriendMoveRequest();
        moveRequest.setRelationshipId(relationship.getRelationshipId());
        moveRequest.setNewGroupId(newGroup.getGroupId());

        FriendRelationship moved = friendService.moveFriend(moveRequest);
        assertEquals(newGroup.getGroupId(), moved.getFriendGroup().getGroupId());
    }

    @Test
    void testResendApply() {
        // 先添加好友再删除
        FriendApplyRequest applyRequest = new FriendApplyRequest();
        applyRequest.setUserId(user1.getUserId());
        applyRequest.setFriendId(user2.getUserId());
        FriendRelationship relationship = friendService.applyFriend(applyRequest);

        FriendHandleRequest handleRequest = new FriendHandleRequest();
        handleRequest.setRelationshipId(relationship.getRelationshipId());
        handleRequest.setAction(1);
        friendService.handleFriendApply(handleRequest);

        friendService.deleteFriend(relationship.getRelationshipId(), user1.getUserId());

        // 重新发送验证
        FriendRelationship resent = friendService.resendApply(relationship.getRelationshipId(), "重新加好友");
        assertEquals(0, resent.getStatus()); // 重新待验证
    }

    @Test
    void testRenameGroup() {
        FriendGroupRequest groupRequest = new FriendGroupRequest();
        groupRequest.setUserId(user1.getUserId());
        groupRequest.setGroupName("家人");
        FriendGroup group = friendService.createGroup(groupRequest);

        FriendGroup renamed = friendService.renameGroup(group.getGroupId(), "亲戚", user1.getUserId());
        assertEquals("亲戚", renamed.getGroupName());
    }

    @Test
    void testDeleteGroup() {
        FriendGroupRequest groupRequest = new FriendGroupRequest();
        groupRequest.setUserId(user1.getUserId());
        groupRequest.setGroupName("临时分组");
        FriendGroup group = friendService.createGroup(groupRequest);

        friendService.deleteGroup(group.getGroupId(), user1.getUserId());

        // 验证分组已被删除
        List<Map<String, Object>> groups = friendService.getGroupsWithFriends(user1.getUserId());
        assertTrue(groups.stream().noneMatch(g -> "临时分组".equals(g.get("groupName"))));
    }
}
