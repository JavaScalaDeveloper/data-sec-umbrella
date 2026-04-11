package com.arelore.data.sec.umbrella.server.core.dto.request;

import com.arelore.data.sec.umbrella.server.core.entity.clickhouse.OfflineScanSnapshotColumnEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 字段级快照分页查询：继承 {@link OfflineScanSnapshotColumnEntity}，并携带筛选与分页条件（不入库）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OfflineScanSnapshotColumnRequest extends OfflineScanSnapshotColumnEntity {

    @TableField(exist = false)
    private String uniqueKeyContains;

    @TableField(exist = false)
    private List<String> sensitivityLevels;

    @TableField(exist = false)
    private String sensitivityTagsContains;

    @TableField(exist = false)
    private int current;

    @TableField(exist = false)
    private int size;
}
