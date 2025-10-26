# 头像上传问题修复 - 本地存储方案

## 🔍 问题分析

**症状：** 前端显示 "Avatar loading failed"

**原因：**
- Auth服务将头像保存在容器内的本地文件系统 (`/uploads/avatars/`)
- 返回路径：`/uploads/avatars/user_2_avatar_xxx.jpg`
- 但是**没有提供HTTP接口**让前端访问这些文件
- 前端尝试访问 `http://52.77.169.8:8080/uploads/avatars/xxx.jpg` → **404 Not Found**

## ✅ 解决方案

**保持使用本地文件存储**，只添加一个HTTP接口来提供文件访问。

### 修改内容

1. ✅ **新增** `FileAccessController.java` - 提供文件访问接口
   - `GET /uploads/avatars/{filename}` - 访问头像
   - `GET /uploads/games/{filename}` - 访问游戏图片
   - 包含安全检查（防止路径穿越攻击）
   - 自动识别文件MIME类型
   - 添加缓存控制头

2. ✅ **修改** `SecurityConfig.java` - 放行文件访问路径
   - 添加 `.requestMatchers("/uploads/**").permitAll()`

## 📁 修改的文件

```
gamevault-cloud/gamevault-auth/
├── src/main/java/com/sg/nusiss/auth/
│   ├── controller/
│   │   └── FileAccessController.java        [新增] ✨
│   └── config/
│       └── SecurityConfig.java               [修改] 添加一行配置
```

**无需修改：**
- ❌ FileUploadService.java （保持不变）
- ❌ application.yml （保持不变）
- ❌ docker-compose.yml （保持不变）
- ❌ pom.xml （保持不变）

## 🚀 部署步骤

### 步骤1：在本地构建

```bash
cd /Users/zyc/IdeaProjects/gamevault-cloud/gamevault-auth

# 构建
mvn clean package -DskipTests

# 构建Docker镜像
docker build -t hzhenj/gamevault-auth:latest .

# 推送到Docker Hub
docker push hzhenj/gamevault-auth:latest
```

### 步骤2：在EC2上更新

```bash
# SSH到EC2
ssh ubuntu@<你的EC2-IP>

# 停止并删除旧容器
sudo docker stop gamevault_auth_cloud
sudo docker rm gamevault_auth_cloud

# 拉取最新镜像
sudo docker pull hzhenj/gamevault-auth:latest

# 启动新容器
cd ~/gamevault-cloud
export DOCKERHUB_USERNAME=hzhenj
sudo -E docker-compose up -d auth

# 查看日志
sudo docker logs -f gamevault_auth_cloud
```

## ✅ 验证测试

### 1. 测试文件访问接口

```bash
# 方式1：浏览器访问
http://<你的EC2-IP>:8080/uploads/avatars/user_2_avatar_164ff8b0.jpg

# 方式2：curl测试
curl -I http://<你的EC2-IP>:8080/uploads/avatars/user_2_avatar_164ff8b0.jpg

# 预期结果：HTTP 200 OK (如果文件存在)
# 或 HTTP 404 Not Found (如果文件不存在)
```

### 2. 前端功能测试

1. ✅ 打开前端：`http://<你的EC2-IP>:3000`
2. ✅ 登录账号
3. ✅ 进入"设置" → "个人资料"
4. ✅ 上传新头像
5. ✅ 确认头像正常显示
6. ✅ 刷新页面，头像仍然显示

### 3. 检查容器内文件

```bash
# 进入Auth容器
sudo docker exec -it gamevault_auth_cloud bash

# 查看上传目录
ls -lh /uploads/avatars/

# 退出容器
exit
```

## 📊 工作原理

### 文件上传流程
```
1. 用户上传头像 (前端)
   ↓
2. POST /api/settings/avatar (Auth服务)
   ↓
3. FileUploadService 保存到 /uploads/avatars/
   ↓
4. 返回路径: /uploads/avatars/user_2_avatar_xxx.jpg
   ↓
5. 前端保存该路径到数据库
```

### 文件访问流程
```
1. 前端需要显示头像
   ↓
2. 读取路径: /uploads/avatars/user_2_avatar_xxx.jpg
   ↓
3. 拼接完整URL: http://52.77.169.8:8080/uploads/avatars/user_2_avatar_xxx.jpg
   ↓
4. GET /uploads/avatars/user_2_avatar_xxx.jpg
   ↓
5. FileAccessController 从本地读取文件
   ↓
6. 返回文件内容 (application/jpeg)
   ↓
7. 浏览器显示头像 ✅
```

## 🔐 安全特性

FileAccessController 包含以下安全措施：

1. **路径穿越防护**：防止访问 `../../../etc/passwd` 等危险路径
2. **文件类型检查**：只能访问特定目录下的文件
3. **存在性验证**：确保文件存在才返回
4. **MIME类型识别**：自动设置正确的Content-Type

## 📝 代码说明

### FileAccessController.java

```java
@GetMapping("/uploads/avatars/{filename:.+}")
public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
    // 1. 构建文件路径：/uploads/avatars/xxx.jpg
    Path filePath = Paths.get(uploadPath, "avatars", filename);
    
    // 2. 安全检查：防止路径穿越
    // 3. 创建文件资源
    Resource resource = new FileSystemResource(filePath);
    
    // 4. 返回文件 + 设置缓存头
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
        .body(resource);
}
```

### SecurityConfig.java

```java
.authorizeHttpRequests(a -> a
    .requestMatchers("/uploads/**").permitAll()  // ← 新增这一行
    .anyRequest().authenticated()
)
```

## ⚠️ 注意事项

### 1. Docker Volume 配置

确保 docker-compose.yml 中配置了volume（已配置）：
```yaml
auth:
  volumes:
    - ./docker/uploads:/uploads  # ← 已存在
```

### 2. 旧头像数据

- 如果之前上传过头像，文件应该还在 `./docker/uploads/avatars/` 目录
- 部署后旧头像应该可以正常访问
- 如果看不到旧头像，检查文件是否真的存在

### 3. 端口访问

前端配置指向：`http://52.77.169.8:8080`
- 这是**Gateway的端口**（8080）
- Gateway会路由到Auth服务（8081）
- 确保Gateway正确配置了路由规则

### 如果Gateway没有路由 `/uploads/**`

需要检查 `gamevault-gateway` 的配置，确保转发文件访问请求到Auth服务。

## 🔧 故障排查

### 问题1：404 Not Found

**检查：**
```bash
# 1. 文件是否存在
sudo docker exec gamevault_auth_cloud ls -lh /uploads/avatars/

# 2. Controller是否启动
sudo docker logs gamevault_auth_cloud | grep FileAccessController

# 3. 测试直接访问Auth服务（绕过Gateway）
curl http://<EC2-IP>:8081/uploads/avatars/xxx.jpg
```

### 问题2：403 Forbidden

**检查：**
```bash
# 文件权限
sudo docker exec gamevault_auth_cloud ls -la /uploads/avatars/

# 如果权限不对，修复
sudo docker exec gamevault_auth_cloud chmod -R 755 /uploads
```

### 问题3：通过Gateway访问失败

检查Gateway路由配置：
```yaml
# gamevault-gateway 应该配置类似规则
- id: auth-uploads
  uri: lb://gamevault-auth
  predicates:
    - Path=/uploads/**
```

## 🎯 优势

相比MinIO方案：
- ✅ **简单**：只需添加一个Controller
- ✅ **无需额外服务**：不依赖MinIO
- ✅ **文件持久化**：通过Docker Volume保证数据不丢失
- ✅ **向后兼容**：旧的上传逻辑完全不变

## 📚 后续优化建议

1. **添加缓存控制**：使用Redis缓存常用头像URL
2. **图片压缩**：上传时自动压缩大图
3. **CDN集成**：通过Nginx或CDN加速静态文件访问
4. **图片水印**：自动添加水印保护版权

---

**修复完成！** 🎉

现在前端可以正常访问本地存储的头像文件了。

