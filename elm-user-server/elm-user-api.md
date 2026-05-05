# ELM User Service API 文档

对应服务：`elm-user-server`

## 1. 基本信息

- 服务名：`elm-user-server`
- 默认数据库：`elm2`
- 统一返回结构（大多数接口）：`CommonResult<T>`

```json
{
  "code": 200,
  "message": "success",
  "result": {}
}
```

说明：`POST /api/auth` 返回 `JwtTokenDto`（不是 `CommonResult`）。

## 2. 鉴权说明

当前项目采用 Bearer Token（JWT）鉴权，需在请求头中携带：

```text
Authorization: Bearer <token>
```

JWT 相关配置（来自配置文件默认值）：
- 普通 token 有效期：`86400s`（1 天）
- rememberMe token 有效期：`2592000s`（30 天）

## 3. 接口列表

### 3.1 用户登录

- 方法：`POST`
- 路径：`/api/auth`
- 鉴权：否
- 请求体：`LoginDto`

```json
{
  "username": "user1001",
  "password": "123456",
  "rememberMe": false
}
```

成功响应：
- HTTP `200`
- Header 包含 `Authorization: Bearer <token>`
- Body：

```json
{
  "id_token": "<jwt>"
}
```

失败响应：
- HTTP `400`：缺少 `username` 或 `password`
- HTTP `401`：用户名或密码错误

### 3.2 用户注册（新接口）

- 方法：`POST`
- 路径：`/api/register`
- 鉴权：否
- 请求体：`RegisterDto`

```json
{
  "userId": "user1001",
  "password": "123456",
  "userName": "张三",
  "userSex": 1
}
```

成功响应（`CommonResult<User>`）：
- `code=200`，`message=register success`
- 返回的 `result.password` 为 `null`

失败响应：
- `code=400`：缺少必填字段
- `code=409`：用户已存在

### 3.3 创建用户（兼容接口）

- 方法：`POST`
- 路径：`/api/users`
- 鉴权：否（当前实现）
- 说明：逻辑与 `/api/register` 完全一致

### 3.4 创建 Person（兼容接口）

- 方法：`POST`
- 路径：`/api/persons`
- 鉴权：否（当前实现）
- 说明：当前实现直接复用 `/api/register`

### 3.5 获取当前登录用户

- 方法：`GET`
- 路径：`/api/user`
- 鉴权：是（Bearer Token）

成功响应：
- `code=200`，`result` 为用户信息（`password=null`）

失败响应：
- `code=401`：token 缺失或非法
- `code=404`：用户不存在

### 3.6 获取用户列表

- 方法：`GET`
- 路径：`/api/users`
- 鉴权：否（当前实现）

成功响应：
- `code=200`
- `result` 为用户数组，所有用户 `password` 都会置空

### 3.7 按用户名查询用户

- 方法：`GET`
- 路径：`/api/userByUsername`
- Query：`userName`
- 鉴权：否（当前实现）

响应：
- `code=200`：成功
- `code=400`：缺少 `userName`
- `code=404`：用户不存在

### 3.8 按 userId 判断是否存在

- 方法：`GET`
- 路径：`/api/userById`
- Query：`userId`
- 鉴权：否（当前实现）

响应：
- `code=200`
- `result` 为 `1`（存在）或 `0`（不存在）
- `code=400`：缺少 `userId`

### 3.9 userId + password 查询用户

- 方法：`GET`
- 路径：`/api/userByIdByPass`
- Query：`userId`, `password`
- 鉴权：否（当前实现）

响应：
- `code=200`：成功，返回用户信息（`password=null`）
- `code=400`：缺少参数
- `code=403`：账号或密码错误

### 3.10 检查用户名是否存在

- 方法：`GET`
- 路径：`/api/userExistsByUsername`
- Query：`userName`
- 鉴权：否（当前实现）

响应：
- `code=200`
- `result` 为 `1`（存在）或 `0`（不存在）
- `code=400`：缺少 `userName`

### 3.11 修改当前用户密码

- 方法：`POST`
- 路径：`/api/password`
- 鉴权：是（Bearer Token）
- 请求体：`PasswordChangeDto`

```json
{
  "currentPassword": "oldPwd",
  "newPassword": "newPwd"
}
```

响应：
- `code=200`：修改成功，`result=1`
- `code=400`：缺少必填字段
- `code=401`：token 缺失或非法
- `code=403`：当前密码错误

### 3.12 更新用户资料

- 方法：`PUT`
- 路径：`/api/users/{userId}`
- 鉴权：是（Bearer Token）
- 权限：token 中的用户只能修改自己的资料
- 请求体：`UserUpdateDto`（可部分更新）

```json
{
  "userName": "Alice",
  "userSex": 1,
  "userImg": "avatar.png"
}
```

响应：
- `code=200`：更新成功，`result=1`
- `code=400`：请求体为空
- `code=401`：token 缺失或非法
- `code=403`：无权限修改该用户
- `code=404`：用户不存在

### 3.13 修改用户名

- 方法：`PUT`
- 路径：`/api/users/{userId}/username`
- 鉴权：是（Bearer Token）
- 权限：token 中的用户只能修改自己的用户名
- 请求体：`UserNameUpdateDto`

```json
{
  "newUsername": "Alice2"
}
```

响应：
- `code=200`：修改成功，`result=1`
- `code=400`：缺少 `newUsername`
- `code=401`：token 缺失或非法
- `code=403`：无权限修改该用户
- `code=404`：用户不存在
- `code=409`：用户名已存在

### 3.14 生日修改资格检查（占位实现）

- 方法：`GET`
- 路径：`/api/user/birthday/modification/check`
- 鉴权：是（Bearer Token）

响应：
- `code=200`
- `result` 结构：

```json
{
  "canModify": true,
  "reason": "birthday-modification-history-not-implemented-yet"
}
```

- `code=401`：token 缺失或非法

### 3.15 旧版兼容接口：按 userId + password 查询用户

- 方法：`GET`
- 路径：`/users/{userId}`
- Query：`password`
- 鉴权：否（兼容旧调用）

响应：
- `code=200`：成功
- `code=400`：缺少 `password`
- `code=403`：密码错误
- `code=404`：用户不存在

### 3.16 旧版兼容接口：判断 userId 是否存在

- 方法：`GET`
- 路径：`/users/exists/{userId}`
- 鉴权：否（兼容旧调用）

响应：
- `code=200`：存在
- `code=404`：不存在

### 3.17 旧版兼容接口：按路径参数注册

- 方法：`POST`
- 路径：`/users/{userId}`
- Content-Type：`application/x-www-form-urlencoded`
- 表单参数：`password`, `userName`, `userSex`
- 鉴权：否（兼容旧调用）

响应：
- `code=200`：注册成功
- `code=400`：缺少必填字段
- `code=409`：用户已存在

## 4. 数据与安全说明

- 新注册用户密码使用 BCrypt 存储。
- 登录时兼容历史明文密码：若检测到库内是明文且登录成功，会自动升级为 BCrypt。
- 对外返回用户信息时，接口会将 `password` 字段置为 `null`。

## 5. 网关路由提示

按当前网关配置，以上 `api` 路径通过 gateway 暴露到 `elm-user-server`。如果网关配置改动，请同步更新本文档。
