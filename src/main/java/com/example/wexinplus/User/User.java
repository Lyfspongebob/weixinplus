package com.example.wexinplus.User;

import com.example.wexinplus.ChatGroup.ChatGroup;
import com.example.wexinplus.FriendGroup.FriendGroup;
import com.example.wexinplus.FriendGroup.FriendRelationship;
import com.example.wexinplus.GroupMember.GroupMember;
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
@Table(name = "users")  // user是MySQL关键字，避免冲突
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String nickname;

    @Column(length = 500)
    private String avatar; // 存储用户头像的图片地址

    private String phone;

    private String email;

    private Integer status; // 0离线 1在线

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    // ========== 好友分组：一个用户拥有多个好友分组 ==========
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FriendGroup> friendGroups;

    // ========== 好友关系（主动发起的）：一个用户可以有多个好友 ==========
    // 通过 FriendRelationship 表中的 user_id 找到"我主动添加的好友"
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FriendRelationship> friendshipsAsUser;

    // ========== 好友关系（被添加的）：一个用户可以被多个人添加为好友 ==========
    // 通过 FriendRelationship 表中的 friend_id 找到"把我当好友的人"
    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FriendRelationship> friendshipsAsFriend;

    // ========== 发送的消息：一个用户可以发送多条消息 ==========
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages;

    // ========== 创建的群聊：一个用户可以创建多个群聊 ==========
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ChatGroup> ownedGroups;

    // ========== 加入的群聊（通过中间表 GroupMember）：一个用户可以加入多个群 ==========
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groupMemberships;

    // ========== 参与的会话（通过中间表 SessionParticipant）：一个用户可以参与多个会话 ==========
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SessionParticipant> sessionParticipants;
}
