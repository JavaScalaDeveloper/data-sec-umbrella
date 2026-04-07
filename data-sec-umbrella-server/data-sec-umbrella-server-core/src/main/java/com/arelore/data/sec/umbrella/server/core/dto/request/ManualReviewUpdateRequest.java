package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

/**
 * 更新 MySQL 库或表资产人工打标的请求体。
 *
 * @author 黄佳豪
 */
@Data
public class ManualReviewUpdateRequest {

    /**
     * 资产主键
     */
    private Long id;

    /**
     * {@link com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum} 的 code；
     * 传空或 null 表示清除人工打标（恢复默认）。
     */
    private String manualReview;
}
