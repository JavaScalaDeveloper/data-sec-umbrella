package com.arelore.data.sec.umbrella.server.core.dto.request;

import com.arelore.data.sec.umbrella.server.core.entity.DataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceRequest extends DataSource {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;
}
