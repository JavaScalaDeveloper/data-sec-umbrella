package com.arelore.data.sec.umbrella.server.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 消息策略ID请求
 */
@Data
public class MessagePolicyIdRequest {
    
    /**
     * 策略ID
     */
    @NotNull(message = "策略ID不能为空")
    private Long id;
}