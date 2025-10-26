package com.sg.nusiss.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 提供上传文件的HTTP访问接口
 */
@Slf4j
@RestController
public class FileAccessController {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    /**
     * 访问头像文件
     * GET /uploads/avatars/{filename}
     */
    @GetMapping("/uploads/avatars/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        return getFile("avatars", filename);
    }

    /**
     * 访问游戏图片文件
     * GET /uploads/games/{filename}
     */
    @GetMapping("/uploads/games/{filename:.+}")
    public ResponseEntity<Resource> getGameImage(@PathVariable String filename) {
        return getFile("games", filename);
    }

    /**
     * 通用文件访问方法
     */
    private ResponseEntity<Resource> getFile(String subDir, String filename) {
        try {
            // 构建文件路径
            Path filePath = Paths.get(uploadPath, subDir, filename);
            
            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 安全检查：防止路径穿越攻击
            Path uploadDir = Paths.get(uploadPath, subDir).toAbsolutePath().normalize();
            Path requestedFile = filePath.toAbsolutePath().normalize();
            if (!requestedFile.startsWith(uploadDir)) {
                log.warn("Path traversal attempt detected: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(filePath);
            
            // 获取文件的MIME类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 返回文件
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400") // 缓存1天
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error reading file: {}/{}", subDir, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

