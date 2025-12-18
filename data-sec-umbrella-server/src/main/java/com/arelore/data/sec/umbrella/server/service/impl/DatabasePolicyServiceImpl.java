package com.arelore.data.sec.umbrella.server.service.impl;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.mapper.DatabasePolicyMapper;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DatabasePolicyServiceImpl extends ServiceImpl<DatabasePolicyMapper, DatabasePolicy> implements DatabasePolicyService {
}