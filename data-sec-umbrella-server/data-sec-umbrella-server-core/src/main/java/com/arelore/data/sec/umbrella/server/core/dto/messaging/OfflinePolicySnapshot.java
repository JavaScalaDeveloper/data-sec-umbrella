package com.arelore.data.sec.umbrella.server.core.dto.messaging;

import lombok.Data;

@Data
public class OfflinePolicySnapshot {
    private Long id;
    private String policyCode;
    private String policyName;
    private String classificationRules;
    private String ruleExpression;
    private String aiRule;
    private String databaseType;
    private Integer sensitivityLevel;
    private Integer hideExample;
}

