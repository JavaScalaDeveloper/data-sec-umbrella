package com.arelore.data.sec.umbrella.server.dto;

import lombok.Data;

@Data
public class OperationResultDTO {
    private boolean success;
    
    public static OperationResultDTO of(boolean success) {
        OperationResultDTO result = new OperationResultDTO();
        result.setSuccess(success);
        return result;
    }
}