package com.example.wexinplus.GroupMember;

import com.example.wexinplus.ChatGroup.ChatGroup;
import com.example.wexinplus.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群成员表（中间表）
 * 记录用户与群聊之间的多对多关系
 *
 * 表关系说明：
 * - 多对一（User）：一个用户可以加入多个群聊
 * - 多对一（ChatGroup）：一个群聊可以有多个成员
 */
@Data
@Entity
@Table(name = "group_member")
public class GroupMember implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    // ========== 多对一：该成员记录关联的用户 ==========
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user;

    // ========== 多对一：该成员记录关联的群聊 ==========
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    @JsonIgnore
    private ChatGroup chatGroup;

    @Column(name = "group_nickname")
    private String groupNickname; // 群昵称（在群里的昵称）

    private Integer role; // 角色：1群主 2管理员 3普通成员

    @Column(name = "join_time")
    private LocalDateTime joinTime = LocalDateTime.now();
}
