package com.arelore.data.sec.umbrella.server.manager.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;
import com.arelore.data.sec.umbrella.server.core.service.DatabasePolicyService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库策略流式服务。
 *
 * @author 黄佳豪
 */
@Service
public class DatabasePolicyStreamService {

    private final DatabasePolicyService databasePolicyService;

    public DatabasePolicyStreamService(DatabasePolicyService databasePolicyService) {
        this.databasePolicyService = databasePolicyService;
    }

    public SseEmitter streamAiRuleDetection(DatabasePolicyRuleDetectionRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L);
        CompletableFuture.runAsync(() -> {
            try {
                DatabasePolicyRuleDetectionResponse response = databasePolicyService.executeAiRuleDetection(request);
                String detail = response.getAiDetail() == null ? "" : response.getAiDetail();
                int step = 24;
                for (int i = 0; i < detail.length(); i += step) {
                    String chunk = detail.substring(i, Math.min(i + step, detail.length()));
                    emitter.send(SseEmitter.event().name("chunk").data(chunk));
                }
                Map<String, Object> donePayload = new LinkedHashMap<>();
                donePayload.put("aiPassed", response.isAiPassed());
                donePayload.put("aiDetail", detail);
                donePayload.put("aiSensitiveSamples", response.getAiSensitiveSamples());
                emitter.send(SseEmitter.event().name("done").data(donePayload));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(ex.getMessage()));
                } catch (Exception ignore) {
                    // ignore
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
}
