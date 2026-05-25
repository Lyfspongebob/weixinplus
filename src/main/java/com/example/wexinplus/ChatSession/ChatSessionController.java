package com.example.wexinplus.ChatSession;

import com.example.wexinplus.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 创建私聊会话
     * POST /api/sessions/createPrivate?userId1=xxx&userId2=xxx
     */
    @PostMapping("/createPrivate")
    public ResponseEntity<ApiResponse<ChatSession>> createPrivateSession(@RequestParam Long userId1,
                                                                          @RequestParam Long userId2) {
        try {
            ChatSession session = chatSessionService.createPrivateSession(userId1, userId2);
            return ResponseEntity.ok(ApiResponse.success("创建会话成功", session));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的所有会话列表
     * GET /api/sessions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserSessions(@PathVariable Long userId) {
        List<Map<String, Object>> sessions = chatSessionService.getUserSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    /**
     * 获取会话的参与者列表
     * GET /api/sessions/participants/{sessionId}
     */
    @GetMapping("/participants/{sessionId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getParticipants(@PathVariable Long sessionId) {
        List<Map<String, Object>> participants = chatSessionService.getSessionParticipants(sessionId);
        return ResponseEntity.ok(ApiResponse.success(participants));
    }

    /**
     * 更新用户最后阅读时间
     * POST /api/sessions/read?userId=xxx&sessionId=xxx
     */
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<Void>> updateReadTime(@RequestParam Long userId,
                                                             @RequestParam Long sessionId) {
        chatSessionService.updateLastReadTime(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success("已更新阅读时间", null));
    }
}
