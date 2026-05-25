package com.example.wexinplus.ChatGroup;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.GroupMember.GroupMember;
import com.example.wexinplus.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "chat_group")
public class ChatGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(length = 1000)
    private String notice; // 群公告

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    // ========== 多对一：一个群聊有一个群主（创建者） ==========
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User owner;

    // ========== 一对多：一个群聊有多个成员（通过中间表 GroupMember） ==========
    @OneToMany(mappedBy = "chatGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> members;

    // ========== 一对一：一个群聊对应一个聊天会话 ==========
    @OneToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    @JsonIgnore
    private ChatSession chatSession;
}
