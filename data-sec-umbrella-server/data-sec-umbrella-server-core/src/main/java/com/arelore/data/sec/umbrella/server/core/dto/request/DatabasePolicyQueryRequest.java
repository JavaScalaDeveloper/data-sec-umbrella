package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库策略查询请求
 *
 * @author arelore
 * @since 2025-12-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DatabasePolicyQueryRequest extends PageRequest {
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
     * 创建人
     */
    private String creator;

    /**
     * 是否隐藏样例：0否，1是
     */
    private Integer hideExample;

    /**
     * 数据库类型（MySQL / Clickhouse / PostgreSQL / Oracle）
     */
    private String databaseType;
}