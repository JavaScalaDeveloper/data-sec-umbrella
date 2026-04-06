package com.arelore.data.sec.umbrella.server.dto.response;

import com.arelore.data.sec.umbrella.server.entity.DataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceResponse extends DataSource {
}
