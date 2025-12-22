package com.arelore.data.sec.umbrella.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponseDTO<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Long pages;
    
    public static <T> PageResponseDTO<T> of(List<T> records, Long total, Integer current, Integer size, Long pages) {
        PageResponseDTO<T> response = new PageResponseDTO<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setCurrent(current);
        response.setSize(size);
        response.setPages(pages);
        return response;
    }
}