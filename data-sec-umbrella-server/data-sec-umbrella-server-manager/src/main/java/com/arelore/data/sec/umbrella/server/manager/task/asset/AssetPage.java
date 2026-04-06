package com.arelore.data.sec.umbrella.server.manager.task.asset;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AssetPage {
    private long total;
    private List<Map<String, Object>> records;
}

