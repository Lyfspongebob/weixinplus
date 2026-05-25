package com.example.wexinplus.ChatSession;

import com.example.wexinplus.ChatGroup.ChatGroup;
import com.example.wexinplus.Message.Message;
import com.example.wexinplus.SessionParticipant.SessionParticipant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "chat_session")
public class ChatSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "session_type")
    private Integer sessionType; // 1=私聊，2=群聊

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    // ========== 一对多：一个会话有多条消息 ==========
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages;

    // ========== 一对一：一个会话对应一个群聊（如果是群聊会话） ==========
    @OneToOne(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ChatGroup chatGroup;

    // ========== 一对多：一个会话有多个参与者（通过中间表 SessionParticipant） ==========
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SessionParticipant> participants;
}
