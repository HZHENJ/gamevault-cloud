package com.sg.nusiss.developer.controller.developer;

import com.sg.nusiss.developer.controller.common.AuthenticatedControllerBase;
import com.sg.nusiss.developer.repository.DevGameAssetRepository;
import com.sg.nusiss.developer.entity.DevGameAsset;
import com.sg.nusiss.developer.service.DevGameStatisticsApplicationService;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/developer/devgameasset")
@RequiredArgsConstructor
public class DevGameAssetDownloadController extends AuthenticatedControllerBase {

    private final DevGameAssetRepository devGameAssetRepository;
    private final DevGameStatisticsApplicationService devGameStatisticsApplicationService;


    @GetMapping("/download/{assetId}")
    public ResponseEntity<Resource> downloadAsset(
            @PathVariable(value = "assetId") String assetId
    ) {
        // 1️⃣ 从数据库查出 asset 信息
        DevGameAsset asset = devGameAssetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        // 2️⃣ 调用统计逻辑（只统计 zip 文件下载）
        if ("zip".equalsIgnoreCase(asset.getAssetType())) {
            devGameStatisticsApplicationService.recordGameDownload(asset.getDevGameId());
        }

        // 2️⃣ 根据文件路径加载文件
        File file = new File(asset.getStoragePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + asset.getStoragePath());
        }

        // 3️⃣ 返回文件流
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + asset.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                .body(resource);
    }
}
