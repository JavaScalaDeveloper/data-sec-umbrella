package com.arelore.data.sec.umbrella.server.core.mapper;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.ApiPolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * API策略Mapper接口
 */
@Mapper
public interface ApiPolicyMapper extends BaseMapper<ApiPolicy> {
}