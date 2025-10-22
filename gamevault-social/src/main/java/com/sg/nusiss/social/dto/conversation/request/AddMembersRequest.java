package com.sg.nusiss.social.dto.conversation.request;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AddMembersRequest
 * @Author HUANG ZHENJIA
 * @Date 2025/10/5
 * @Description
 */
@Data
public class AddMembersRequest {
    private List<Long> userIds;
}
