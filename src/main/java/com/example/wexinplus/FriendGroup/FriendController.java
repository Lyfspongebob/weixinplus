package com.example.wexinplus.FriendGroup;

import com.example.wexinplus.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // ==================== 好友分组管理 ====================

    /**
     * 创建好友分组
     * POST /api/friends/groups/create
     */
    @PostMapping("/groups/create")
    public ResponseEntity<ApiResponse<FriendGroup>> createGroup(@RequestBody FriendGroupRequest request) {
        try {
            FriendGroup group = friendService.createGroup(request);
            return ResponseEntity.ok(ApiResponse.success("创建分组成功", group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的所有好友分组（含好友列表）
     * GET /api/friends/groups/{userId}
     */
    @GetMapping("/groups/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGroups(@PathVariable Long userId) {
        List<Map<String, Object>> groups = friendService.getGroupsWithFriends(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * 删除好友分组
     * DELETE /api/friends/groups/delete/{groupId}?userId=xxx
     */
    @DeleteMapping("/groups/delete/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId,
                                                          @RequestParam Long userId) {
        try {
            friendService.deleteGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success("删除分组成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重命名好友分组
     * PUT /api/friends/groups/rename/{groupId}?userId=xxx&newName=xxx
     */
    @PutMapping("/groups/rename/{groupId}")
    public ResponseEntity<ApiResponse<FriendGroup>> renameGroup(@PathVariable Long groupId,
                                                                 @RequestParam Long userId,
                                                                 @RequestParam String newName) {
        try {
            FriendGroup group = friendService.renameGroup(groupId, newName, userId);
            return ResponseEntity.ok(ApiResponse.success("重命名成功", group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 好友关系管理 ====================

    /**
     * 发送好友申请
     * POST /api/friends/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<FriendRelationship>> applyFriend(@RequestBody FriendApplyRequest request) {
        try {
            FriendRelationship relationship = friendService.applyFriend(request);
            return ResponseEntity.ok(ApiResponse.success("好友申请已发送", relationship));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 处理好友申请（通过/拒绝）
     * POST /api/friends/handle
     */
    @PostMapping("/handle")
    public ResponseEntity<ApiResponse<FriendRelationship>> handleApply(@RequestBody FriendHandleRequest request) {
        try {
            FriendRelationship relationship = friendService.handleFriendApply(request);
            String msg = request.getAction() == 1 ? "已通过好友申请" : "已拒绝好友申请";
            return ResponseEntity.ok(ApiResponse.success(msg, relationship));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除好友
     * DELETE /api/friends/delete/{relationshipId}?userId=xxx
     */
    @DeleteMapping("/delete/{relationshipId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(@PathVariable Long relationshipId,
                                                           @RequestParam Long userId) {
        try {
            friendService.deleteFriend(relationshipId, userId);
            return ResponseEntity.ok(ApiResponse.success("删除好友成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 移动好友到其他分组
     * PUT /api/friends/move
     */
    @PutMapping("/move")
    public ResponseEntity<ApiResponse<FriendRelationship>> moveFriend(@RequestBody FriendMoveRequest request) {
        try {
            FriendRelationship relationship = friendService.moveFriend(request);
            return ResponseEntity.ok(ApiResponse.success("移动成功", relationship));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重新发送验证信息
     * POST /api/friends/resend/{relationshipId}?applyMessage=xxx
     */
    @PostMapping("/resend/{relationshipId}")
    public ResponseEntity<ApiResponse<FriendRelationship>> resendApply(@PathVariable Long relationshipId,
                                                                        @RequestParam String applyMessage) {
        try {
            FriendRelationship relationship = friendService.resendApply(relationshipId, applyMessage);
            return ResponseEntity.ok(ApiResponse.success("验证信息已重新发送", relationship));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户收到的待处理好友申请
     * GET /api/friends/pending/{userId}
     */
    @GetMapping("/pending/{userId}")
    public ResponseEntity<ApiResponse<List<FriendRelationship>>> getPendingRequests(@PathVariable Long userId) {
        List<FriendRelationship> requests = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * 获取用户的所有好友列表
     * GET /api/friends/list/{userId}
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllFriends(@PathVariable Long userId) {
        List<Map<String, Object>> friends = friendService.getAllFriends(userId);
        return ResponseEntity.ok(ApiResponse.success(friends));
    }
}
