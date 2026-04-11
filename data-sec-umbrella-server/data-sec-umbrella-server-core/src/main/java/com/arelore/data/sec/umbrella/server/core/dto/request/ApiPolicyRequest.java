package com.arelore.data.sec.umbrella.server.core.dto.request;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.ApiPolicy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiPolicyRequest extends ApiPolicy {
}