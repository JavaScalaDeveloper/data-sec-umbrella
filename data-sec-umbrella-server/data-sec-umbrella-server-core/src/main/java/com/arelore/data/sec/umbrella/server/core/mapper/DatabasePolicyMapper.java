package com.arelore.data.sec.umbrella.server.core.mapper;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DatabasePolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatabasePolicyMapper extends BaseMapper<DatabasePolicy> {
}