package com.example.wexinplus.FriendGroup;

import com.example.wexinplus.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "friend_group")
public class FriendGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    // ========== 多对一：多个好友分组属于同一个用户 ==========
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    // ========== 一对多：一个好友分组下可以有多个好友关系 ==========
    // 通过 FriendRelationship 表中的 friendGroup 字段关联
    @OneToMany(mappedBy = "friendGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FriendRelationship> friendRelationships;
}
