package com.example.wexinplus.SessionParticipant;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话参与者表（中间表）
 * 记录用户与聊天会话之间的多对多关系
 * 私聊和群聊都通过这张表来记录"谁参与了哪个会话"
 *
 * 表关系说明：
 * - 多对一（User）：一个用户可以参与多个会话
 * - 多对一（ChatSession）：一个会话可以有多个参与者
 */
@Data
@Entity
@Table(name = "session_participant")
public class SessionParticipant implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    // ========== 多对一：参与会话的用户 ==========
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user;

    // ========== 多对一：用户参与的会话 ==========
    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    @JsonIgnore
    private ChatSession chatSession;

    @Column(name = "join_time")
    private LocalDateTime joinTime = LocalDateTime.now();

    @Column(name = "last_read_time")
    private LocalDateTime lastReadTime; // 用户最后一次阅读该会话消息的时间，用于未读计数
}
