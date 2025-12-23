package com.arelore.data.sec.umbrella.server.dto.request;

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
}