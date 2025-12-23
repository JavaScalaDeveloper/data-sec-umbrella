package com.arelore.data.sec.umbrella.server.dto.request;

import lombok.Data;

/**
 * 消息策略查询请求
 *
 * @author arelore
 * @since 2025-12-24
 */
@Data
public class MessagePolicyQueryRequest {
    /**
     * 策略ID
     */
    private Long id;
    
    /**
     * 策略编码
     */
    private String policyCode;
}