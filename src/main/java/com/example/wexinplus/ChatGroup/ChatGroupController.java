package com.example.wexinplus.ChatGroup;

import com.example.wexinplus.GroupMember.GroupMember;
import com.example.wexinplus.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class ChatGroupController {

    @Autowired
    private ChatGroupService chatGroupService;

    /**
     * 创建群聊
     * POST /api/groups/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ChatGroup>> createGroup(@RequestBody CreateGroupRequest request) {
        try {
            ChatGroup group = chatGroupService.createGroup(request);
            return ResponseEntity.ok(ApiResponse.success("创建群聊成功", group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 添加群成员
     * POST /api/groups/addMembers
     */
    @PostMapping("/addMembers")
    public ResponseEntity<ApiResponse<List<GroupMember>>> addMembers(@RequestBody AddGroupMemberRequest request) {
        try {
            List<GroupMember> newMembers = chatGroupService.addMembers(request);
            return ResponseEntity.ok(ApiResponse.success("添加成员成功", newMembers));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 移除群成员
     * DELETE /api/groups/removeMember/{groupId}/{memberId}?operatorId=xxx
     */
    @DeleteMapping("/removeMember/{groupId}/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long groupId,
                                                           @PathVariable Long memberId,
                                                           @RequestParam Long operatorId) {
        try {
            chatGroupService.removeMember(groupId, memberId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("移除成员成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 退出群聊
     * POST /api/groups/leave/{groupId}?userId=xxx
     */
    @PostMapping("/leave/{groupId}")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId,
                                                         @RequestParam Long userId) {
        try {
            chatGroupService.leaveGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success("退出群聊成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 解散群聊
     * DELETE /api/groups/disband/{groupId}?ownerId=xxx
     */
    @DeleteMapping("/disband/{groupId}")
    public ResponseEntity<ApiResponse<Void>> disbandGroup(@PathVariable Long groupId,
                                                           @RequestParam Long ownerId) {
        try {
            chatGroupService.disbandGroup(groupId, ownerId);
            return ResponseEntity.ok(ApiResponse.success("解散群聊成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取群聊信息（含成员列表）
     * GET /api/groups/info/{groupId}
     */
    @GetMapping("/info/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGroupInfo(@PathVariable Long groupId) {
        try {
            Map<String, Object> info = chatGroupService.getGroupInfo(groupId);
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索群聊
     * GET /api/groups/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ChatGroup>>> searchGroups(@RequestParam String keyword) {
        List<ChatGroup> groups = chatGroupService.searchGroups(keyword);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 获取用户加入的所有群聊
     * GET /api/groups/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserGroups(@PathVariable Long userId) {
        List<Map<String, Object>> groups = chatGroupService.getUserGroups(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 更新群公告
     * PUT /api/groups/notice/{groupId}?operatorId=xxx&notice=xxx
     */
    @PutMapping("/notice/{groupId}")
    public ResponseEntity<ApiResponse<ChatGroup>> updateNotice(@PathVariable Long groupId,
                                                                @RequestParam Long operatorId,
                                                                @RequestParam String notice) {
        try {
            ChatGroup group = chatGroupService.updateNotice(groupId, notice, operatorId);
            return ResponseEntity.ok(ApiResponse.success("更新公告成功", group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
