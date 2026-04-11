package com.arelore.data.sec.umbrella.server.core.mapper;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.MessagePolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息策略Mapper接口
 */
@Mapper
public interface MessagePolicyMapper extends BaseMapper<MessagePolicy> {
}