package com.sg.nusiss.auth.controller;

import com.sg.nusiss.auth.dto.UserDTO;
import com.sg.nusiss.auth.entity.User;
import com.sg.nusiss.auth.repository.UserRepository;
import com.sg.nusiss.common.domain.BaseResponse;
import com.sg.nusiss.common.domain.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public UserDTO getUserById(@PathVariable(value = "userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: userId=" + userId));

        return convertToDTO(user);
    }

    /**
     * 批量查询用户信息（供其他服务调用）
     */
    @PostMapping("/batch")
    public List<UserDTO> getUsersByIds(@RequestBody List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户名模糊搜索（供其他服务调用）
     */
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam(value = "keyword", required = true)String keyword) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}