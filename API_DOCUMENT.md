# Wexinplus 后端接口文档

> 基础URL: `http://localhost:8083`
> 所有接口返回统一格式: `ApiResponse<T>`

## 统一响应格式

```json
{
  "code": 200,       // 状态码: 200成功, 400业务错误, 401未授权, 403禁止, 404未找到, 500服务器错误
  "message": "success",
  "data": {}         // 具体数据
}
```

---

## 一、用户模块 (User)

### 1.1 用户注册

- **URL**: `POST /api/users/register`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "username": "string (必填, 唯一)",
  "password": "string (必填)",
  "nickname": "string (选填, 默认为用户名)",
  "phone": "string (选填)",
  "email": "string (选填)"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": null,
    "phone": "13800138000",
    "email": "test@example.com",
    "status": 0,
    "createTime": "2026-05-25T10:00:00"
  }
}
```

### 1.2 用户登录

- **URL**: `POST /api/users/login`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "username": "string (必填)",
  "password": "string (必填)"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "status": 1,
    ...
  }
}
```

### 1.3 用户登出

- **URL**: `POST /api/users/logout/{userId}`

**路径参数**: `userId` - 用户ID

**响应**: `{ "code": 200, "message": "登出成功", "data": null }`

### 1.4 获取用户信息

- **URL**: `GET /api/users/{userId}`

**路径参数**: `userId` - 用户ID

### 1.5 搜索用户

- **URL**: `GET /api/users/search?username=xxx`

**查询参数**: `username` - 用户名

### 1.6 更新用户信息

- **URL**: `PUT /api/users/update`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "userId": 1,
  "nickname": "新昵称",
  "phone": "13900139000",
  "email": "new@example.com",
  "avatar": "/uploads/avatars/xxx.jpg"
}
```

---

## 二、好友模块 (Friend)

### 2.1 创建好友分组

- **URL**: `POST /api/friends/groups/create`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "userId": 1,
  "groupName": "家人"
}
```

### 2.2 获取好友分组（含好友列表）

- **URL**: `GET /api/friends/groups/{userId}`

**路径参数**: `userId` - 用户ID

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "groupId": 1,
      "groupName": "我的好友",
      "friends": [
        {
          "relationshipId": 1,
          "userId": 2,
          "username": "friend1",
          "nickname": "好友1",
          "avatar": null,
          "status": 1,
          "remark": "备注"
        }
      ]
    },
    {
      "groupId": 2,
      "groupName": "家人",
      "friends": []
    }
  ]
}
```

### 2.3 删除好友分组

- **URL**: `DELETE /api/friends/groups/delete/{groupId}?userId=xxx`

**路径参数**: `groupId` - 分组ID
**查询参数**: `userId` - 用户ID

> 注意: 删除分组后，该分组下的好友会自动移动到"我的好友"分组

### 2.4 重命名好友分组

- **URL**: `PUT /api/friends/groups/rename/{groupId}?userId=xxx&newName=xxx`

**路径参数**: `groupId` - 分组ID
**查询参数**: `userId` - 用户ID, `newName` - 新分组名称

### 2.5 发送好友申请

- **URL**: `POST /api/friends/apply`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "userId": 1,
  "friendId": 2,
  "groupId": 1,
  "applyMessage": "你好，加个好友吧",
  "remark": "备注名"
}
```

> 状态说明: 0=待验证, 1=已通过, 2=已拒绝, 3=已删除

### 2.6 处理好友申请

- **URL**: `POST /api/friends/handle`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "relationshipId": 1,
  "action": 1,
  "remark": "备注名"
}
```

> `action`: 1=通过, 2=拒绝

### 2.7 删除好友

- **URL**: `DELETE /api/friends/delete/{relationshipId}?userId=xxx`

**路径参数**: `relationshipId` - 好友关系ID
**查询参数**: `userId` - 用户ID

### 2.8 移动好友到其他分组

- **URL**: `PUT /api/friends/move`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "relationshipId": 1,
  "newGroupId": 2
}
```

### 2.9 重新发送验证信息

- **URL**: `POST /api/friends/resend/{relationshipId}?applyMessage=xxx`

**路径参数**: `relationshipId` - 好友关系ID
**查询参数**: `applyMessage` - 验证信息

### 2.10 获取待处理好友申请

- **URL**: `GET /api/friends/pending/{userId}`

**路径参数**: `userId` - 用户ID

### 2.11 获取好友列表

- **URL**: `GET /api/friends/list/{userId}`

**路径参数**: `userId` - 用户ID

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "relationshipId": 1,
      "userId": 2,
      "username": "friend1",
      "nickname": "好友1",
      "avatar": null,
      "status": 1,
      "remark": "备注",
      "groupId": 1,
      "groupName": "我的好友"
    }
  ]
}
```

---

## 三、群聊模块 (Group)

### 3.1 创建群聊

- **URL**: `POST /api/groups/create`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "ownerId": 1,
  "groupName": "Java学习群",
  "notice": "欢迎加入",
  "memberIds": [2, 3]
}
```

### 3.2 添加群成员

- **URL**: `POST /api/groups/addMembers`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "groupId": 1,
  "operatorId": 1,
  "memberIds": [4, 5]
}
```

> `operatorId` 必须是群主或管理员

### 3.3 移除群成员

- **URL**: `DELETE /api/groups/removeMember/{groupId}/{memberId}?operatorId=xxx`

**路径参数**: `groupId` - 群ID, `memberId` - 被移除的用户ID
**查询参数**: `operatorId` - 操作人ID

### 3.4 退出群聊

- **URL**: `POST /api/groups/leave/{groupId}?userId=xxx`

**路径参数**: `groupId` - 群ID
**查询参数**: `userId` - 用户ID

> 群主不能退出，只能解散群聊

### 3.5 解散群聊

- **URL**: `DELETE /api/groups/disband/{groupId}?ownerId=xxx`

**路径参数**: `groupId` - 群ID
**查询参数**: `ownerId` - 群主ID

### 3.6 获取群聊信息

- **URL**: `GET /api/groups/info/{groupId}`

**路径参数**: `groupId` - 群ID

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "groupId": 1,
    "groupName": "Java学习群",
    "notice": "欢迎加入",
    "createTime": "2026-05-25T10:00:00",
    "ownerId": 1,
    "ownerName": "admin",
    "sessionId": 1,
    "members": [
      {
        "memberId": 1,
        "userId": 1,
        "username": "admin",
        "nickname": "管理员",
        "avatar": null,
        "groupNickname": null,
        "role": 1,
        "joinTime": "2026-05-25T10:00:00"
      }
    ],
    "memberCount": 1
  }
}
```

> 角色说明: 1=群主, 2=管理员, 3=普通成员

### 3.7 搜索群聊

- **URL**: `GET /api/groups/search?keyword=xxx`

**查询参数**: `keyword` - 搜索关键词

### 3.8 获取用户加入的群聊

- **URL**: `GET /api/groups/user/{userId}`

**路径参数**: `userId` - 用户ID

### 3.9 更新群公告

- **URL**: `PUT /api/groups/notice/{groupId}?operatorId=xxx&notice=xxx`

**路径参数**: `groupId` - 群ID
**查询参数**: `operatorId` - 操作人ID, `notice` - 公告内容

---

## 四、会话模块 (Session)

### 4.1 创建私聊会话

- **URL**: `POST /api/sessions/createPrivate?userId1=xxx&userId2=xxx`

**查询参数**: `userId1` - 用户1ID, `userId2` - 用户2ID

> 如果两个用户之间已存在私聊会话，会返回现有会话（幂等）

### 4.2 获取用户会话列表

- **URL**: `GET /api/sessions/user/{userId}`

**路径参数**: `userId` - 用户ID

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "sessionId": 1,
      "sessionType": 1,
      "createTime": "2026-05-25T10:00:00",
      "lastReadTime": "2026-05-25T10:30:00",
      "targetUserId": 2,
      "targetUsername": "friend1",
      "targetNickname": "好友1",
      "targetAvatar": null
    },
    {
      "sessionId": 2,
      "sessionType": 2,
      "createTime": "2026-05-25T11:00:00",
      "lastReadTime": null,
      "groupId": 1,
      "groupName": "Java学习群"
    }
  ]
}
```

> `sessionType`: 1=私聊, 2=群聊

### 4.3 获取会话参与者

- **URL**: `GET /api/sessions/participants/{sessionId}`

**路径参数**: `sessionId` - 会话ID

### 4.4 更新阅读时间

- **URL**: `POST /api/sessions/read?userId=xxx&sessionId=xxx`

**查询参数**: `userId` - 用户ID, `sessionId` - 会话ID

---

## 五、消息模块 (Message)

### 5.1 发送消息

- **URL**: `POST /api/messages/send`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "sessionId": 1,
  "senderId": 1,
  "content": "你好",
  "messageType": 1,
  "filePath": null
}
```

> `messageType`: 1=文本, 2=图片, 3=语音, 4=文件

### 5.2 获取会话消息列表

- **URL**: `GET /api/messages/session/{sessionId}`

**路径参数**: `sessionId` - 会话ID

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "messageId": 1,
      "sessionId": 1,
      "senderId": 1,
      "senderName": "用户1",
      "senderAvatar": null,
      "content": "你好",
      "messageType": 1,
      "filePath": null,
      "sendTime": "2026-05-25T10:00:00",
      "status": 1
    }
  ]
}
```

### 5.3 按条件查询消息

- **URL**: `POST /api/messages/query`
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "sessionId": 1,
  "startTime": "2026-05-25T00:00:00",
  "endTime": "2026-05-25T23:59:59",
  "messageType": 1
}
```

> 所有字段均为可选，至少需要 `sessionId`

### 5.4 下载聊天记录

- **URL**: `GET /api/messages/download/{sessionId}?startTime=xxx&endTime=xxx`

**路径参数**: `sessionId` - 会话ID
**查询参数**:
- `startTime` (选填): 开始时间, 格式 `yyyy-MM-dd HH:mm:ss`
- `endTime` (选填): 结束时间, 格式 `yyyy-MM-dd HH:mm:ss`

**响应**: 返回下载文件的服务器路径

### 5.5 上传文件

- **URL**: `POST /api/messages/upload`
- **Content-Type**: `multipart/form-data`

**请求参数**:
- `file`: 文件 (必填)
- `type`: 文件类型 (选填, 默认`file`, 可选值: `image`, `voice`, `file`)

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "filePath": "/uploads/images/1685000000_1234.jpg",
    "fileName": "photo.jpg",
    "fileSize": "102400"
  }
}
```

---

## 六、数据字典

### 6.1 用户状态 (User.status)
| 值 | 说明 |
|----|------|
| 0  | 离线 |
| 1  | 在线 |

### 6.2 好友关系状态 (FriendRelationship.status)
| 值 | 说明 |
|----|------|
| 0  | 待验证 |
| 1  | 已通过 |
| 2  | 已拒绝 |
| 3  | 已删除 |

### 6.3 消息类型 (Message.messageType)
| 值 | 说明 |
|----|------|
| 1  | 文本 |
| 2  | 图片 |
| 3  | 语音 |
| 4  | 文件 |

### 6.4 消息状态 (Message.status)
| 值 | 说明 |
|----|------|
| 1  | 已发送 |
| 2  | 已接收 |
| 3  | 已读 |

### 6.5 会话类型 (ChatSession.sessionType)
| 值 | 说明 |
|----|------|
| 1  | 私聊 |
| 2  | 群聊 |

### 6.6 群成员角色 (GroupMember.role)
| 值 | 说明 |
|----|------|
| 1  | 群主 |
| 2  | 管理员 |
| 3  | 普通成员 |

---

## 七、错误码说明

| HTTP状态码 | code | 说明 |
|-----------|------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 业务错误（如用户名已存在、密码错误等） |
| 401 | 401 | 未授权 |
| 403 | 403 | 无权限 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |

---

## 八、前端调用建议

1. **用户登录后**，前端应保存 `userId` 和 `username`，后续所有操作都需要用户ID
2. **创建私聊会话**时，如果已存在会话会返回现有会话，无需重复创建
3. **发送消息**时，`messageType=3` 表示语音消息，`filePath` 存储语音文件路径
4. **上传文件**接口支持图片、语音、文件的上传，上传后返回的 `filePath` 可直接用于发送消息
5. **下载聊天记录**返回的是服务器文件路径，前端可通过 `/downloads/chat_records/xxx.txt` 访问
