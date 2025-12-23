package com.arelore.data.sec.umbrella.server.controller;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.dto.request.MessagePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.MessagePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.MessagePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.service.MessagePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 消息策略表 前端控制器
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@RestController
@RequestMapping("api/message-policy")
public class MessagePolicyController {

    private final MessagePolicyService messagePolicyService;

    @Autowired
    public MessagePolicyController(MessagePolicyService messagePolicyService) {
        this.messagePolicyService = messagePolicyService;
    }

    @PostMapping("/list")
    public Result<PageResponse<MessagePolicyResponse>> list(@RequestBody MessagePolicyQueryRequest request) {
        PageResponse<MessagePolicyResponse> pageResponse = messagePolicyService.getPage(request);
        return Result.success(pageResponse);
    }

    @PostMapping("/getById")
    public Result<MessagePolicyResponse> getById(@RequestBody MessagePolicyQueryRequest request) {
        MessagePolicyResponse messagePolicy = messagePolicyService.getById(request.getId());
        if (messagePolicy != null) {
            return Result.success(messagePolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/getByPolicyCode")
    public Result<MessagePolicyResponse> getByPolicyCode(@RequestBody MessagePolicyQueryRequest request) {
        MessagePolicyResponse messagePolicy = messagePolicyService.getByPolicyCode(request.getPolicyCode());
        if (messagePolicy != null) {
            return Result.success(messagePolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/create")
    public Result<Long> create(@RequestBody MessagePolicyRequest messagePolicyRequest) {
        Long id = messagePolicyService.create(messagePolicyRequest);
        return Result.success(id);
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody MessagePolicyRequest messagePolicyRequest) {
        boolean result = messagePolicyService.update(messagePolicyRequest.getId(), messagePolicyRequest);
        if (result) {
            return Result.success(true);
        }
        return Result.error("更新失败，策略不存在");
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody MessagePolicyQueryRequest request) {
        boolean result = messagePolicyService.delete(request.getId());
        if (result) {
            return Result.success(true);
        }
        return Result.error("删除失败，策略不存在");
    }
}