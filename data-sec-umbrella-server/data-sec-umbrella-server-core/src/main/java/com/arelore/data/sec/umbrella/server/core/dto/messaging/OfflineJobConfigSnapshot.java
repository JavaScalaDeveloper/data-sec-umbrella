package com.arelore.data.sec.umbrella.server.core.dto.messaging;

import lombok.Data;

@Data
public class OfflineJobConfigSnapshot {
    private Integer sampleCount;
    private String sampleMode;
    private Integer enableSampling;
    private Integer enableAiScan;
    private String scanPeriod;
    private String supportedTags;
    private String scanScope;
    private String scanInstanceIds;
    private String timeRangeType;
}

