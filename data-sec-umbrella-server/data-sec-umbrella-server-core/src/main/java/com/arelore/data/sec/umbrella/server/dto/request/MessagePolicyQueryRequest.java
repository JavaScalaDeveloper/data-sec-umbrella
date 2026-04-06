package com.arelore.data.sec.umbrella.server.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息策略查询请求
 *
 * @author arelore
 * @since 2025-12-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePolicyQueryRequest extends PageRequest {
    /**
     * 策略ID
     */
    private Long id;
    
    /**
     * 策略编码
     */
    private String policyCode;
    
    /**
     * 策略名称
     */
    private String policyName;
}