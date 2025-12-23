package com.arelore.data.sec.umbrella.server.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Long pages;
    
    public static <T> PageResponse<T> of(List<T> records, Long total, Integer current, Integer size, Long pages) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setCurrent(current);
        response.setSize(size);
        response.setPages(pages);
        return response;
    }
}