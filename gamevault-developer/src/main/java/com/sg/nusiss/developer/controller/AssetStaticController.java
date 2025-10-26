package com.sg.nusiss.developer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Paths;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetStaticController {

    @Value("${app.asset-storage-path}")
    private String assetStoragePath;

    /**
     * 静态资源访问接口
     * 访问路径: /assets/{userId}/{gameId}/{fileName}
     */
    @GetMapping("/{userId}/{gameId}/{fileName:.+}")
    public ResponseEntity<Resource> getAsset(
            @PathVariable String userId,
            @PathVariable String gameId,
            @PathVariable String fileName) {

        // 构建文件路径
        String filePath = Paths.get(assetStoragePath, userId, gameId, fileName).toString();
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // 返回文件
        Resource resource = new FileSystemResource(file);

        // 根据文件扩展名设置 Content-Type
        String contentType = getContentType(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * 根据文件扩展名返回 MIME 类型
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }
}