package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.dto.DatabasePolicyQueryDTO;
import com.arelore.data.sec.umbrella.server.dto.PageResponseDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DatabasePolicyService extends IService<DatabasePolicy> {
    PageResponseDTO<DatabasePolicy> listPoliciesWithPagination(DatabasePolicyQueryDTO queryDTO);
}