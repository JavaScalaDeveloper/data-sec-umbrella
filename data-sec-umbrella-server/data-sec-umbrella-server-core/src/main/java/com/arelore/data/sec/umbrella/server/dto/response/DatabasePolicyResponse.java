package com.arelore.data.sec.umbrella.server.dto.response;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatabasePolicyResponse extends DatabasePolicy {
}