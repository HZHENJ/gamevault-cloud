package com.sg.nusiss.social.controller.conversation;

import com.sg.nusiss.common.domain.BaseResponse;
import com.sg.nusiss.common.domain.ResultUtils;
import com.sg.nusiss.common.security.SecurityUtils;
import com.sg.nusiss.social.dto.conversation.request.AddMembersRequest;
import com.sg.nusiss.social.dto.conversation.request.CreateConversationRequest;
import com.sg.nusiss.social.dto.conversation.request.DissolveRequest;
import com.sg.nusiss.social.dto.conversation.response.ConversationListResponse;
import com.sg.nusiss.social.dto.conversation.response.CreateConversationResponse;
import com.sg.nusiss.social.dto.conversation.response.DissolveConversationResponse;
import com.sg.nusiss.social.dto.conversation.response.MemberResponse;
import com.sg.nusiss.social.entity.conversation.Conversation;
import com.sg.nusiss.social.service.conversation.ConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName ConversationController
 * @Author HUANG ZHENJIA
 * @Date 2025/9/30
 * @Description
 */

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Create a conversation
     */
    @PostMapping("/create")
    public BaseResponse<CreateConversationResponse> createConversation(
            @RequestBody CreateConversationRequest request) {

        // get ownerId from JWT
        Long ownerId = SecurityUtils.getCurrentUserId();

        // create conversation
        Conversation conversation = conversationService.createConversation(
                request.getTitle(),
                ownerId
        );

        return ResultUtils.success(new CreateConversationResponse(conversation.getId()));
    }

    /**
     * List all conversation for current user
     */
    @GetMapping("/list")
    public BaseResponse<List<ConversationListResponse>> getUserConversations() {
        // get ownerId from JWT
        Long userId = SecurityUtils.getCurrentUserId();

        // get all conversations for current user
        List<ConversationListResponse> conversations =
                conversationService.getUserConversations(userId);

        return ResultUtils.success(conversations);
    }

    /**
     * dissolve conversation (only owner can do)
     * */
    @PostMapping("/dissolve")
    public BaseResponse<DissolveConversationResponse> dissolveConversation(
            @RequestBody DissolveRequest request) {
        // get ownerId from JWT
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // dissolve conversation (only owner)
        conversationService.dissolveConversation(
                request.getConversationId(),
                currentUserId
        );

        return ResultUtils.success(null);
    }

    /**
     * Get all users inside conversation
     */
    @GetMapping("/{conversationId}/members")
    public BaseResponse<List<MemberResponse>> getMembers(
            @PathVariable(value = "conversationId") Long conversationId
    ) {
        // 从 JWT 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 获取成员列表
        List<MemberResponse> members = conversationService.getMembers(conversationId, currentUserId);

        return ResultUtils.success(members);
    }

    /**
     * 添加成员到群聊
     */
    @PostMapping("/{conversationId}/members/add")
    public BaseResponse<Void> addMembers(
            @PathVariable(value = "conversationId") Long conversationId,
            @RequestBody AddMembersRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        conversationService.addMembers(conversationId, request.getUserIds(), currentUserId);
        return ResultUtils.success(null);
    }
}
