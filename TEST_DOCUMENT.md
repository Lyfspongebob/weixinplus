# Wexinplus 后端测试文档

## 测试环境

- **数据库**: MySQL (localhost:3306/weixinplus)
- **服务器端口**: 8083
- **测试框架**: JUnit 5 + Spring Boot Test
- **事务管理**: 每个测试方法自动回滚 (`@Transactional`)

---

## 测试用例清单

### 1. 用户模块测试 (UserServiceTest)

| 编号 | 测试方法 | 测试场景 | 预期结果 |
|------|---------|---------|---------|
| UT-01 | testRegister_Success | 正常注册新用户 | 注册成功，返回用户信息，status=0 |
| UT-02 | testRegister_DuplicateUsername | 注册已存在的用户名 | 抛出异常"用户名已存在" |
| UT-03 | testLogin_Success | 正确用户名密码登录 | 登录成功，status变为1 |
| UT-04 | testLogin_WrongPassword | 错误密码登录 | 抛出异常"用户名或密码错误" |
| UT-05 | testLogin_UserNotFound | 不存在的用户登录 | 抛出异常"用户名或密码错误" |
| UT-06 | testLogout | 用户登出 | status变为0 |
| UT-07 | testGetUserById | 通过ID获取用户 | 返回正确的用户信息 |
| UT-08 | testGetUserById_NotFound | 获取不存在的用户 | 抛出异常"用户不存在" |
| UT-09 | testUpdateUser | 更新用户信息 | 昵称和手机号更新成功 |

### 2. 好友模块测试 (FriendServiceTest)

| 编号 | 测试方法 | 测试场景 | 预期结果 |
|------|---------|---------|---------|
| FT-01 | testCreateGroup_Success | 创建好友分组 | 分组创建成功 |
| FT-02 | testCreateGroup_DuplicateName | 创建同名分组 | 抛出异常"分组名称已存在" |
| FT-03 | testGetGroupsWithFriends | 获取分组列表 | 默认有"我的好友"分组 |
| FT-04 | testApplyAndHandleFriend | 发送并通过好友申请 | 双向好友关系建立成功 |
| FT-05 | testRejectFriendApply | 拒绝好友申请 | 状态变为2(已拒绝) |
| FT-06 | testDeleteFriend | 删除好友 | 好友列表中不再包含对方 |
| FT-07 | testMoveFriend | 移动好友到其他分组 | 好友的groupId更新 |
| FT-08 | testResendApply | 重新发送验证信息 | 状态变为0(待验证) |
| FT-09 | testRenameGroup | 重命名分组 | 分组名称更新 |
| FT-10 | testDeleteGroup | 删除分组 | 分组被删除 |

### 3. 群聊模块测试 (ChatGroupServiceTest)

| 编号 | 测试方法 | 测试场景 | 预期结果 |
|------|---------|---------|---------|
| GT-01 | testCreateGroup_Success | 创建群聊 | 群聊创建成功，自动创建会话 |
| GT-02 | testCreateGroup_WithMembers | 创建群聊并添加成员 | 群主+2成员=3人 |
| GT-03 | testAddMembers | 添加群成员 | 成功添加2名成员 |
| GT-04 | testRemoveMember | 移除群成员 | 只剩群主1人 |
| GT-05 | testLeaveGroup | 成员退出群聊 | 只剩群主1人 |
| GT-06 | testOwnerCannotLeave | 群主退出群聊 | 抛出异常 |
| GT-07 | testDisbandGroup | 解散群聊 | 群聊被删除 |
| GT-08 | testGetGroupInfo | 获取群信息 | 返回正确的群信息和成员列表 |
| GT-09 | testUpdateNotice | 更新群公告 | 公告内容更新 |
| GT-10 | testSearchGroups | 搜索群聊 | 返回匹配的群聊 |
| GT-11 | testGetUserGroups | 获取用户群聊列表 | 返回用户加入的所有群聊 |

### 4. 会话模块测试 (ChatSessionServiceTest)

| 编号 | 测试方法 | 测试场景 | 预期结果 |
|------|---------|---------|---------|
| ST-01 | testCreatePrivateSession | 创建私聊会话 | 会话创建成功，type=1 |
| ST-02 | testCreatePrivateSession_ReuseExisting | 重复创建会话 | 返回同一会话 |
| ST-03 | testGetUserSessions | 获取用户会话列表 | 返回1个会话 |
| ST-04 | testGetSessionParticipants | 获取会话参与者 | 返回2个参与者 |
| ST-05 | testUpdateLastReadTime | 更新阅读时间 | lastReadTime不为空 |

### 5. 消息模块测试 (MessageServiceTest)

| 编号 | 测试方法 | 测试场景 | 预期结果 |
|------|---------|---------|---------|
| MT-01 | testSendMessage_Text | 发送文本消息 | 消息保存成功 |
| MT-02 | testSendMessage_Voice | 发送语音消息 | 语音消息保存成功 |
| MT-03 | testGetSessionMessages | 获取会话消息列表 | 返回2条消息 |
| MT-04 | testQueryMessages_ByTimeRange | 按时间范围查询 | 只返回时间范围内的消息 |
| MT-05 | testQueryMessages_ByType | 按消息类型查询 | 只返回指定类型的消息 |
| MT-06 | testDownloadChatRecord | 下载全部聊天记录 | 文件生成成功 |
| MT-07 | testDownloadChatRecord_WithTimeRange | 按时间范围下载 | 文件生成成功 |
| MT-08 | testSendMessage_ToGroupSession | 发送群消息 | 群消息保存成功 |

---

## 运行测试

### 使用 Gradle 运行所有测试

```bash
./gradlew test
```

### 运行单个测试类

```bash
./gradlew test --tests "com.example.wexinplus.UserServiceTest"
./gradlew test --tests "com.example.wexinplus.FriendServiceTest"
./gradlew test --tests "com.example.wexinplus.ChatGroupServiceTest"
./gradlew test --tests "com.example.wexinplus.ChatSessionServiceTest"
./gradlew test --tests "com.example.wexinplus.MessageServiceTest"
```

### 在 IDE 中运行测试

在 IntelliJ IDEA 或 VS Code 中，直接点击测试类或测试方法旁边的运行按钮即可。

---

## 测试数据准备

测试类使用 `@BeforeEach` 在每个测试方法前自动创建测试数据：

1. **UserServiceTest**: 创建一个测试用户
2. **FriendServiceTest**: 创建两个测试用户
3. **ChatGroupServiceTest**: 创建一个群主和两个成员
4. **ChatSessionServiceTest**: 创建两个测试用户
5. **MessageServiceTest**: 创建两个测试用户和一个私聊会话

所有测试使用 `@Transactional` 注解，测试完成后自动回滚数据，不会影响数据库。

---

## 测试覆盖的功能点

### 登录与注册
- [x] 用户注册（成功/重复用户名）
- [x] 用户登录（成功/密码错误/用户不存在）
- [x] 用户登出
- [x] 获取用户信息
- [x] 搜索用户
- [x] 更新用户信息

### 聊天管理
- [x] 私聊会话创建（幂等）
- [x] 群聊会话创建
- [x] 发送文本消息
- [x] 发送语音消息（+5分功能）
- [x] 查询聊天记录（按时间/类型）
- [x] 下载聊天记录到本地
- [x] 获取会话列表
- [x] 获取会话参与者

### 好友管理
- [x] 创建好友分组
- [x] 删除好友分组（自动迁移好友）
- [x] 重命名好友分组
- [x] 发送好友申请
- [x] 通过/拒绝好友申请
- [x] 删除好友
- [x] 移动好友到其他分组
- [x] 重新发送验证信息
- [x] 获取待处理申请
- [x] 获取好友列表

### 群聊管理
- [x] 创建群聊
- [x] 添加/移除群成员
- [x] 退出/解散群聊
- [x] 获取群信息
- [x] 搜索群聊
- [x] 更新群公告
