package com.arelore.data.sec.umbrella.server.core.dto.request;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DatabasePolicy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatabasePolicyRequest extends DatabasePolicy {
}