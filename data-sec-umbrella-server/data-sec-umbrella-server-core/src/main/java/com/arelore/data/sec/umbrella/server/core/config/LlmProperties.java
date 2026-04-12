package com.arelore.data.sec.umbrella.server.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置（Ollama）。
 *
 * @author 黄佳豪
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm.ollama")
public class LlmProperties {

    /**
     * 是否启用 LLM 推理。
     */
    private boolean enabled = false;

    /**
     * Ollama 服务地址。
     */
    private String baseUrl = "http://book-n95:11434";

    /**
     * 模型名称。
     */
    private String modelName = "qwen3.5:0.8b";

    /**
     * 主模型不可用时自动降级使用的模型名称。
     */
    private String fallbackModelName = "qwen3:0.6b";

    /**
     * 推理超时时间（秒）；慢模型或大提示词时可配合 {@code LLM_OLLAMA_TIMEOUT_SECONDS} 再调大。
     */
    private Integer timeoutSeconds = 600;
}

