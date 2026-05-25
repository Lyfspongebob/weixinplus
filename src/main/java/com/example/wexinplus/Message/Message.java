package com.example.wexinplus.Message;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.User.User;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    // ========== 多对一：多条消息属于同一个会话 ==========
    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ChatSession chatSession;

    // ========== 多对一：多条消息由同一个用户发送 ==========
    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;

    private String content;

    @Column(name = "message_type")
    private Integer messageType; // 1.text 2.image 3.voice 4.file

    @Column(name = "send_time")
    private LocalDateTime sendTime = LocalDateTime.now();

    private Integer status; // 1.sent 2.received 3.read

    @Column(name = "file_path")
    private String filePath; // 文件/图片/语音的存储路径
}
