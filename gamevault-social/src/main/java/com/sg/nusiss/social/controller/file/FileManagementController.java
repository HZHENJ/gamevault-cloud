package com.sg.nusiss.social.controller.file;

import com.sg.nusiss.common.security.SecurityUtils;
import com.sg.nusiss.common.domain.BaseResponse;
import com.sg.nusiss.common.domain.ResultUtils;
import com.sg.nusiss.social.dto.file.request.FileDeleteRequest;
import com.sg.nusiss.social.dto.file.request.FileDownloadRequest;
import com.sg.nusiss.social.dto.file.request.FileListRequest;
import com.sg.nusiss.social.dto.file.response.FileDownloadResponse;
import com.sg.nusiss.social.dto.file.response.FileInfoResponse;
import com.sg.nusiss.social.service.file.FileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName FileManagementController
 * @Author HUANG ZHENJIA
 * @Date 2025/10/19
 * @Description
 */

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileManagementController {

    private final FileManagementService fileManagementService;

    public FileManagementController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    public BaseResponse<FileInfoResponse> getFileInfo(@PathVariable(value = "fileId") String fileId) {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        FileInfoResponse response = fileManagementService.getFileInfo(fileId, userId);
        return ResultUtils.success(response);
    }

    /**
     * 生成文件下载URL
     */
    @PostMapping("/download")
    public BaseResponse<FileDownloadResponse> getDownloadUrl(
            @Valid @RequestBody FileDownloadRequest request,
            HttpServletRequest httpRequest) {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(httpRequest);

        FileDownloadResponse response = fileManagementService.getDownloadUrl(request, userId, ipAddress);
        return ResultUtils.success(response);
    }

    /**
     * 查询文件列表
     */
    @PostMapping("/list")
    public BaseResponse<Page<FileInfoResponse>> listFiles(
            @RequestBody FileListRequest request) {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        Page<FileInfoResponse> response = fileManagementService.listFiles(request, userId);
        return ResultUtils.success(response);
    }

    /**
     * 根据业务类型和业务ID查询文件列表
     */
    @GetMapping("/list/{bizType}/{bizId}")
    public BaseResponse<Page<FileInfoResponse>> listFilesByBiz(
            @PathVariable(value = "bizType") String bizType,
            @PathVariable(value = "bizId") String bizId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        FileListRequest request = FileListRequest.builder()
                .bizType(bizType)
                .bizId(bizId)
                .page(page)
                .size(size)
                .status(1) // 只查询正常文件
                .build();

        Page<FileInfoResponse> response = fileManagementService.listFiles(request, userId);
        return ResultUtils.success(response);
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public BaseResponse<Void> deleteFile(
            @Valid @RequestBody FileDeleteRequest request,
            HttpServletRequest httpRequest) {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(httpRequest);

        fileManagementService.deleteFile(request, userId, ipAddress);
        return ResultUtils.success(null);
    }

    /**
     * 批量删除文件
     */
    @PostMapping("/delete/batch")
    public BaseResponse<Void> batchDeleteFiles(
            @RequestBody List<String> fileIds,
            @RequestParam(value = "physicalDelete", defaultValue = "false") Boolean physicalDelete)  {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        fileManagementService.batchDeleteFiles(fileIds, userId, physicalDelete);
        return ResultUtils.success(null);
    }

    /**
     * 获取用户文件统计信息
     */
    @GetMapping("/stats")
    public BaseResponse<FileManagementService.UserFileStats> getUserFileStats() {

        // 从 JWT 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        FileManagementService.UserFileStats stats = fileManagementService.getUserFileStats(userId);
        return ResultUtils.success(stats);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
