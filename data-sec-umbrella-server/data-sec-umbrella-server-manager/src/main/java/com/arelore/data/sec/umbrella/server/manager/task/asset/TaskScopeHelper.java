package com.arelore.data.sec.umbrella.server.manager.task.asset;

import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskScopeHelper {

    public List<String> parseStringList(String jsonOrNull) {
        if (!StringUtils.hasText(jsonOrNull)) {
            return new ArrayList<>();
        }
        try {
            List<String> arr = JSON.parseArray(jsonOrNull.trim(), String.class);
            return arr != null ? arr : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

