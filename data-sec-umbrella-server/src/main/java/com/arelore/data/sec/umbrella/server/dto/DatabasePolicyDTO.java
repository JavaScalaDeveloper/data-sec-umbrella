package com.arelore.data.sec.umbrella.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DatabasePolicyDTO {
    private Long id;
    private String policyCode;
    private String policyName;
    private String policyDescription;
    private Integer sensitivityLevel;
    private Integer hideExample;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}