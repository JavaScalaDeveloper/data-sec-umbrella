package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

/**
 * 分页查询请求
 *
 * @author arelore
 * @since 2025-12-24
 */
@Data
public class PageRequest {
    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;
    
    /**
     * 实例（域名:端口）
     */
    private String instance;
    
    /**
     * 数据库名
     */
    private String databaseName;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 敏感等级
     */
    private String sensitivityLevel;
    
    /**
     * 敏感标签
     */
    private String sensitivityTags;
}