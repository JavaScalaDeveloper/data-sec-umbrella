package com.arelore.data.sec.umbrella.server.core.dto.response;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.MessagePolicy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePolicyResponse extends MessagePolicy {
}