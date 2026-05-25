package com.example.wexinplus.FriendGroup;

import com.example.wexinplus.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系表（中间表）
 * 记录用户之间的好友关系，以及该好友属于哪个分组
 *
 * 表关系说明：
 * - 多对一（User）：一个用户可以添加多个好友（user_id -> 主动添加方）
 * - 多对一（User）：一个用户可以被多个人添加为好友（friend_id -> 被添加方）
 * - 多对一（FriendGroup）：一个分组下可以有多个好友
 */
@Data
@Entity
@Table(name = "friend_relationship")
public class FriendRelationship implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long relationshipId;

    // ========== 多对一：主动添加好友的用户 ==========
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user;

    // ========== 多对一：被添加为好友的用户 ==========
    @ManyToOne
    @JoinColumn(name = "friend_id", referencedColumnName = "user_id")
    private User friend;

    // ========== 多对一：该好友属于哪个分组 ==========
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    private FriendGroup friendGroup;

    @Column(name = "remark")
    private String remark; // 好友备注（给好友起的别名）

    private Integer status; // 0待验证 1已通过 2已拒绝 3已删除

    @Column(name = "apply_message")
    private String applyMessage; // 验证信息/申请留言

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime = LocalDateTime.now();
}
