package com.sg.nusiss.shopping.controller;

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
 */
@Slf4j
@RestController
public class FileAccessController {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    /**
     * 访问游戏图片
     */
    @GetMapping("/uploads/games/{filename:.+}")
    public ResponseEntity<Resource> getGameImage(@PathVariable String filename) {
        return getFile("games", filename);
    }

    /**
     * 通用文件访问
     */
    private ResponseEntity<Resource> getFile(String subDir, String filename) {
        try {
            Path filePath = Paths.get(uploadPath, subDir, filename);
            
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 安全检查：防止路径穿越
            Path uploadDir = Paths.get(uploadPath, subDir).toAbsolutePath().normalize();
            Path requestedFile = filePath.toAbsolutePath().normalize();
            if (!requestedFile.startsWith(uploadDir)) {
                log.warn("Path traversal attempt: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Resource resource = new FileSystemResource(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error reading file: {}/{}", subDir, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

