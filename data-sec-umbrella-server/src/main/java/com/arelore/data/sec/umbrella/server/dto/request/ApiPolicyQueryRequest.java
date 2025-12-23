package com.arelore.data.sec.umbrella.server.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API策略查询请求
 *
 * @author arelore
 * @since 2025-12-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiPolicyQueryRequest extends PageRequest {
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
    
    /**
     * 敏感等级
     */
    private Integer sensitivityLevel;
}