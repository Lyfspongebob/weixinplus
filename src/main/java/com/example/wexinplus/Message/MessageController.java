package com.example.wexinplus.Message;

import com.example.wexinplus.dto.ApiResponse;
import com.example.wexinplus.dto.QueryMessageRequest;
import com.example.wexinplus.dto.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * 发送消息
     * POST /api/messages/send
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Message>> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            Message message = messageService.sendMessage(request);
            return ResponseEntity.ok(ApiResponse.success("发送成功", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取会话的消息列表
     * GET /api/messages/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSessionMessages(
            @PathVariable Long sessionId) {
        List<Map<String, Object>> messages = messageService.getSessionMessages(sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * 按条件查询消息
     * POST /api/messages/query
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> queryMessages(
            @RequestBody QueryMessageRequest request) {
        List<Map<String, Object>> messages = messageService.queryMessages(request);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * 下载聊天记录
     * GET /api/messages/download/{sessionId}
     * 可选参数: startTime, endTime (格式: yyyy-MM-dd HH:mm:ss)
     */
//    @GetMapping("/download/{sessionId}")
//    public ResponseEntity<ApiResponse<String>> downloadChatRecord(
//            @PathVariable Long sessionId,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
//        try {
//            String filePath = messageService.downloadChatRecord(sessionId, startTime, endTime);
//            return ResponseEntity.ok(ApiResponse.success("导出成功", filePath));
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
    @GetMapping("/download/{sessionId}")
    public ResponseEntity<Resource> downloadChatRecord(
            @PathVariable Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Path filePath = messageService.downloadChatRecord(sessionId, startTime, endTime);
            java.io.File file = filePath.toFile();

            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(file.length())
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 上传文件（图片/语音/文件）
     * POST /api/messages/upload?type=image|voice|file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "file") String type) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请选择文件"));
        }

        try {
            // 确定子目录
            String subDir;
            switch (type) {
                case "image": subDir = "images/"; break;
                case "voice": subDir = "voices/"; break;
                default: subDir = "files/";
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000) + extension;

            // 保存文件
            Path uploadPath = Paths.get(UPLOAD_DIR + subDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 返回访问路径
            String accessPath = "/uploads/" + subDir + filename;
            Map<String, String> result = Map.of(
                    "filePath", accessPath,
                    "fileName", originalFilename != null ? originalFilename : filename,
                    "fileSize", String.valueOf(file.getSize())
            );

            return ResponseEntity.ok(ApiResponse.success("上传成功", result));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }
}
