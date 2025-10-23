package com.sg.nusiss.auth.dto;

/**
 * @ClassName UserDTO
 * @Author HUANG ZHENJIA
 * @Date 2025/10/23
 * @Description
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
}
