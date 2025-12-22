package com.arelore.data.sec.umbrella.server.dto;

import lombok.Data;

@Data
public class DatabasePolicyQueryDTO {
    private String policyCode;
    private String policyName;
    private Integer sensitivityLevel;
    private Integer hideExample;
    private Integer current = 1;
    private Integer size = 10;
}