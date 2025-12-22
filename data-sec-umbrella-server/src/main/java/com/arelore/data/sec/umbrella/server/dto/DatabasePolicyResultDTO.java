package com.arelore.data.sec.umbrella.server.dto;

import lombok.Data;

@Data
public class DatabasePolicyResultDTO {
    private boolean success;
    private DatabasePolicyDTO data;
    
    public static DatabasePolicyResultDTO of(boolean success, DatabasePolicyDTO data) {
        DatabasePolicyResultDTO result = new DatabasePolicyResultDTO();
        result.setSuccess(success);
        result.setData(data);
        return result;
    }
}