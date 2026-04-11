package com.arelore.data.sec.umbrella.server.core.dto.response;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceResponse extends DataSource {
}
